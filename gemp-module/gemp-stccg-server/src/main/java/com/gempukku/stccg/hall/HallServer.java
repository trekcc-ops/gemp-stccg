package com.gempukku.stccg.hall;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.SubscriptionConflictException;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.ChatRoomMediator;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.CloseableReadLock;
import com.gempukku.stccg.common.CloseableWriteLock;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.GameParticipant;
import com.gempukku.stccg.game.GameResultListener;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.tournament.Tournament;
import com.gempukku.stccg.tournament.TournamentQueue;
import com.gempukku.stccg.tournament.TournamentQueueCallback;
import com.gempukku.stccg.tournament.TournamentService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class HallServer extends AbstractServer {

    private static final int TICK_COUNTER_START = 60;
    private static final int PLAYER_TABLE_INACTIVITY_PERIOD = 1000 * 20 ; // 20 seconds
    private static final int PLAYER_CHAT_INACTIVITY_PERIOD = 1000 * 60 * 5; // 5 minutes

    @SuppressWarnings("SpellCheckingInspection")
    private static final String DEFAULT_MESSAGE_OF_THE_DAY = "Lorem ipsum dolor sit amet, " +
            "consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    private static final long SCHEDULED_TOURNAMENT_LOAD_TIME = 1000 * 60 * 60 * 24 * 7; // 1 week
    private final CollectionsManager _collectionsManager;
    private String _messageOfTheDay = DEFAULT_MESSAGE_OF_THE_DAY;
    private boolean _shutdown;
    private final ReadWriteLock _hallDataAccessLock = new ReentrantReadWriteLock(false);
    private final CloseableReadLock _readLock = new CloseableReadLock(_hallDataAccessLock);
    private final CloseableWriteLock _writeLock = new CloseableWriteLock(_hallDataAccessLock);
    private final TableHolder _tableHolder;
    private final Map<User, HallCommunicationChannel> _playerChannelCommunication = new ConcurrentHashMap<>();
    private final Map<String, Tournament> _runningTournaments = new LinkedHashMap<>();
    private final Map<String, TournamentQueue> _tournamentQueues = new LinkedHashMap<>();
    private final ChatRoomMediator _hallChat;
    private int _tickCounter = TICK_COUNTER_START;
    private final TournamentService _tournamentService;
    private final GameServer _gameServer;

    public HallServer(CollectionsManager collectionsManager, TournamentService tournamentService, GameServer gameServer,
                      ChatRoomMediator hallChat, TableHolder tableHolder) {
        _tournamentService = tournamentService;
        _collectionsManager = collectionsManager;
        _tableHolder = tableHolder;
        _hallChat = hallChat;
        _gameServer = gameServer;
    }

    final void hallChanged() {
        for (HallCommunicationChannel commChannel : _playerChannelCommunication.values())
            commChannel.hallChanged();
    }

    private void doAfterStartup() {
        for (Tournament tournament : _tournamentService.getLiveTournaments())
            _runningTournaments.put(tournament.getTournamentId(), tournament);
    }

    public final void setShutdown(boolean shutdown, ChatServer chatServer) throws SQLException, IOException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            boolean cancelMessage = _shutdown && !shutdown;
            _shutdown = shutdown;
            if (shutdown) {
                _tableHolder.cancelWaitingTables();
                cancelTournamentQueues();
                chatServer.sendSystemMessageToAllUsers(
                        "System is entering shutdown mode and will be restarted when all games are finished.");
                hallChanged();
            }
            else if(cancelMessage){
                chatServer.sendSystemMessageToAllUsers(
                        "Shutdown mode canceled; games may now resume.");
            }
        }
    }

    public final String getDailyMessage() {
        try (CloseableReadLock ignored = _readLock.open()) {
            return _messageOfTheDay;
        }
    }

    public final void setDailyMessage(String message) {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            _messageOfTheDay = message;
            hallChanged();
        }
    }

    public final int getTablesCount() {
        try (CloseableReadLock ignored = _readLock.open()) {
            return _tableHolder.getTableCount();
        }
    }

    private void cancelTournamentQueues() throws SQLException, IOException {
        for (TournamentQueue tournamentQueue : _tournamentQueues.values())
            tournamentQueue.leaveAllPlayers(_collectionsManager);
    }

    public final void createNewTable(User player, GameSettings gameSettings, CardDeck cardDeck, GameServer gameServer,
                                     LeagueService leagueService)
            throws HallException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            GameParticipant participant = new GameParticipant(player, cardDeck);
            _tableHolder.validatePlayerForLeague(player.getName(), gameSettings);
            final GameTable table = _tableHolder.createTable(gameSettings, participant);
            table.createGameIfFull(gameServer, this, leagueService);
            hallChanged();
        }
    }

    public void createTournamentGameInternal(GameServer gameServer, GameSettings gameSettings,
                                             GameParticipant[] participants,
                                             String tournamentName, GameResultListener listener) {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            if (!_shutdown) {
                final GameTable gameTable = _tableHolder.createTable(gameSettings, participants);
                List<GameResultListener> listenerList = List.of(listener,
                        new NotifyHallListenersGameResultListener(this));
                gameTable.createTournamentGameInternal(gameServer, listenerList, tournamentName);
            }
        }
    }

    public final void addPlayerToQueue(String queueId, User player, String deckName, CardBlueprintLibrary cardLibrary,
                                       DeckDAO deckDAO)
            throws HallException, SQLException, IOException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            TournamentQueue tournamentQueue = _tournamentQueues.get(queueId);
            if (tournamentQueue == null)
                throw new HallException(
                        "Tournament queue already finished accepting players, try again in a few seconds");
            CardDeck cardDeck = null;
            if (tournamentQueue.isRequiresDeck())
                cardDeck = validateUserAndDeck(tournamentQueue.getGameFormat(), player, deckName,
                        cardLibrary, deckDAO);
            if (tournamentQueue.isPlayerSignedUp(player.getName()))
                throw new HallException("You have already joined that queue");
            tournamentQueue.joinPlayer(_collectionsManager, player, cardDeck);

            hallChanged();
        }
    }

    public final void joinTableAsPlayer(String tableId, User player, User deckOwner, String deckName,
                                        CardBlueprintLibrary cardBlueprintLibrary, DeckDAO deckDAO,
                                        LeagueService leagueService, GameServer gameServer)
            throws HallException {
        if (_shutdown)
            throw new HallException(
                    "Server is in shutdown mode. Server will be restarted after all running games are finished.");

        GameSettings gameSettings = _tableHolder.getGameSettings(tableId);
        CardDeck cardDeck = validateUserAndDeck(gameSettings.getGameFormat(), deckOwner, deckName,
                cardBlueprintLibrary, deckDAO);

        try (CloseableWriteLock ignored = _writeLock.open()) {
            _tableHolder.joinTable(tableId, player, cardDeck, gameServer, this, leagueService);
        }
    }


    public final void removePlayerFromQueue(String queueId, User player) throws SQLException, IOException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            TournamentQueue tournamentQueue = _tournamentQueues.get(queueId);
            if (tournamentQueue != null && tournamentQueue.isPlayerSignedUp(player.getName())) {
                tournamentQueue.leavePlayer(_collectionsManager, player);
                hallChanged();
            }
        }
    }

    private boolean removePlayerFromAllQueues(User player) throws SQLException, IOException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            boolean result = false;
            for (TournamentQueue tournamentQueue : _tournamentQueues.values()) {
                if (tournamentQueue.isPlayerSignedUp(player.getName())) {
                    tournamentQueue.leavePlayer(_collectionsManager, player);
                    result = true;
                }
            }
            return result;
        }
    }

    public final void dropFromTournament(String tournamentId, User player) {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            Tournament tournament = _runningTournaments.get(tournamentId);
            if (tournament != null) {
                tournament.dropPlayer(player.getName());
                hallChanged();
            }
        }
    }

    public final void leaveAwaitingTable(User player, String tableId) {
        _hallDataAccessLock.writeLock().lock();
        try {
            _tableHolder.leaveAwaitingTable(player, tableId);
            hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    private boolean leaveAwaitingTablesForLeavingPlayer(User player) {
        _hallDataAccessLock.writeLock().lock();
        try {
            return _tableHolder.leaveAwaitingTablesForPlayer(player.getName());
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public final HallCommunicationChannel signupUserForHallAndGetChannel(User player) {
        try (CloseableReadLock ignored = _readLock.open()) {
            HallCommunicationChannel channel = new HallCommunicationChannel();
            _playerChannelCommunication.put(player, channel);
            return channel;
        }
    }

    public final HallCommunicationChannel getCommunicationChannel(User player, int channelNumber)
            throws HttpProcessingException {
        _hallDataAccessLock.readLock().lock();
        try {
            HallCommunicationChannel communicationChannel = _playerChannelCommunication.get(player);
            if (communicationChannel != null) {
                if (communicationChannel.getChannelNumber() == channelNumber) {
                    return communicationChannel;
                } else {
                    throw new SubscriptionConflictException();
                }
            } else {
                throw new SubscriptionExpiredException();
            }
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }


    final void processHall(User player, Map<String, Map<String, String>> tournamentQueuesOnServer,
                           Set<String> playedGamesOnServer, Map<String, GameTableView> tablesOnServer,
                           Map<String, Map<String, String>> tournamentsOnServer, Map<Object, Object> itemsToSerialize) {
        try (CloseableReadLock ignored = _readLock.open()) {
            String currentTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            itemsToSerialize.put("serverTime", currentTime);
            if (_messageOfTheDay != null)
                itemsToSerialize.put("messageOfTheDay", _messageOfTheDay);

            playedGamesOnServer.addAll(_tableHolder.getPlayedGames(player));
            tablesOnServer.putAll(_tableHolder.getSerializedTables(player));

            for (Map.Entry<String, TournamentQueue> tournamentQueueEntry : _tournamentQueues.entrySet()) {
                String tournamentQueueKey = tournamentQueueEntry.getKey();
                TournamentQueue tournamentQueue = tournamentQueueEntry.getValue();
                Map<String, String> props = tournamentQueue.serializeForHall(player.getName());
                tournamentQueuesOnServer.put(tournamentQueueKey, props);
            }

            for (Map.Entry<String, Tournament> tournamentEntry : _runningTournaments.entrySet()) {
                String tournamentKey = tournamentEntry.getKey();
                Tournament tournament = tournamentEntry.getValue();
                Map<String, String> props = tournament.serializeForHall(player.getName());
                tournamentsOnServer.put(tournamentKey, props);
            }
        }
    }

    public CardDeck validateUserAndDeck(GameFormat format, User player, String deckName,
                                         CardBlueprintLibrary cardBlueprintLibrary,
                                         DeckDAO deckDAO) throws HallException {
        CardDeck cardDeck = deckDAO.getDeckForUser(player, deckName);
        if (cardDeck == null)
            throw new HallException("You don't have a deck registered yet");

        CardDeck deck = format.applyErrata(cardBlueprintLibrary, cardDeck);
        List<String> validations = format.validateDeck(cardBlueprintLibrary, deck);
        if(!validations.isEmpty()) {
            String firstValidation = validations.stream().findFirst().orElse(null);
            long newLineCount = firstValidation.chars().filter(x -> x == '\n').count();
            if (firstValidation.contains("\n"))
                firstValidation = firstValidation.substring(0, firstValidation.indexOf("\n"));
            long issueCount = validations.size() + newLineCount;
            StringBuilder validationMessage = new StringBuilder();
            validationMessage.append("Your selected deck is incompatible with the '");
            validationMessage.append(format.getName()).append("' format. ");
            if (issueCount <= 1) {
                validationMessage.append(firstValidation);
            } else {
                validationMessage.append("Issues include: '").append(firstValidation).append("' and ");
                validationMessage.append(issueCount - 1).append(" other issues.");
            }
            throw new HallException(validationMessage.toString());
        }
        return cardDeck;
    }


    @Override
    protected final void cleanup() throws SQLException, IOException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            _tableHolder.removeFinishedGames();
            removeInactivePlayers();
            updateTournamentQueues();
            updateRunningTournaments();
        }
    }

    private void removeInactivePlayers() throws SQLException, IOException {
        long currentTime = System.currentTimeMillis();
        Map<User, HallCommunicationChannel> visitCopy = new LinkedHashMap<>(_playerChannelCommunication);
        for (Map.Entry<User, HallCommunicationChannel> lastVisitedPlayer : visitCopy.entrySet()) {
            if (currentTime > lastVisitedPlayer.getValue().getLastAccessed() + PLAYER_TABLE_INACTIVITY_PERIOD) {
                User player = lastVisitedPlayer.getKey();
                boolean leftTables = leaveAwaitingTablesForLeavingPlayer(player);
                boolean leftQueues = removePlayerFromAllQueues(player);
                if (leftTables || leftQueues)
                    hallChanged();
            }
            if (currentTime > lastVisitedPlayer.getValue().getLastAccessed() + PLAYER_CHAT_INACTIVITY_PERIOD) {
                User player = lastVisitedPlayer.getKey();
                _playerChannelCommunication.remove(player);
            }
        }
    }

    public final void startServer() {
        basicStartup();
        doAfterStartup();
    }

    public boolean isShutdown() {
        return _shutdown;
    }

    private void updateTournamentQueues() throws SQLException, IOException {
        for (Map.Entry<String, TournamentQueue> runningTournamentQueue :
                new HashMap<>(_tournamentQueues).entrySet()) {
            String tournamentQueueKey = runningTournamentQueue.getKey();
            TournamentQueue tournamentQueue = runningTournamentQueue.getValue();
            TournamentQueueCallback queueCallback = new HallTournamentQueueCallback(_runningTournaments);
            tournamentQueue.process(queueCallback, _collectionsManager, _tournamentService);
            // If it's finished, remove it
            if (tournamentQueue.shouldBeRemovedFromHall()) {
                _tournamentQueues.remove(tournamentQueueKey);
                hallChanged();
            }
        }

        if (_tickCounter == TICK_COUNTER_START) {
            _tickCounter = 0;
            long nextLoadTime = System.currentTimeMillis() + SCHEDULED_TOURNAMENT_LOAD_TIME;
            Map<String, TournamentQueue> queuesToAdd =
                    _tournamentService.getFutureScheduledTournamentQueuesNotInHall(nextLoadTime, _tournamentQueues);
            if (!queuesToAdd.isEmpty()) {
                _tournamentQueues.putAll(queuesToAdd);
                hallChanged();
            }
        }
        _tickCounter++;
    }

    private void updateRunningTournaments() {
        for (Map.Entry<String, Tournament> tournamentEntry : new HashMap<>(_runningTournaments).entrySet()) {
            Tournament runningTournament = tournamentEntry.getValue();
            GameFormat tournamentFormat = runningTournament.getGameFormat();
            HallTournamentCallback callback = new HallTournamentCallback(this,
                    _gameServer, runningTournament, tournamentFormat, _hallChat);
            boolean changed = runningTournament.advanceTournament(callback, _collectionsManager);
            if (runningTournament.getTournamentStage() == Tournament.Stage.FINISHED)
                _runningTournaments.remove(tournamentEntry.getKey());
            if (changed)
                hallChanged();
        }
    }

}