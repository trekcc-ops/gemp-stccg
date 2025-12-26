package com.gempukku.stccg.hall;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.SubscriptionConflictException;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.chat.ChatCommandErrorException;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.chat.HallChatRoomMediator;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.CloseableReadLock;
import com.gempukku.stccg.common.CloseableWriteLock;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.database.DeckDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.service.AdminService;
import com.gempukku.stccg.tournament.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class HallServer extends AbstractServer {

    private static final Logger LOGGER = LogManager.getLogger(HallServer.class);
    private static final int TICK_COUNTER_START = 60;
    private static final int HALL_TIMEOUT_PERIOD = 300;
    private static final int PLAYER_TABLE_INACTIVITY_PERIOD = 1000 * 20 ; // 20 seconds
    private static final int PLAYER_CHAT_INACTIVITY_PERIOD = 1000 * 60 * 5; // 5 minutes
    // Repeat tournaments every 2 days

    @SuppressWarnings("SpellCheckingInspection")
    private static final String DEFAULT_MESSAGE_OF_THE_DAY = "Lorem ipsum dolor sit amet, " +
            "consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    private static final long SCHEDULED_TOURNAMENT_LOAD_TIME = 1000 * 60 * 60 * 24 * 7; // 1 week
    private final FormatLibrary _formatLibrary;
    private final CollectionsManager _collectionsManager;
    private String _messageOfTheDay = DEFAULT_MESSAGE_OF_THE_DAY;
    private boolean _shutdown;
    private final ReadWriteLock _hallDataAccessLock = new ReentrantReadWriteLock(false);
    private final CloseableReadLock _readLock = new CloseableReadLock(_hallDataAccessLock);
    private final CloseableWriteLock _writeLock = new CloseableWriteLock(_hallDataAccessLock);
    private final TableHolder tableHolder;
    private final Map<User, HallCommunicationChannel> _playerChannelCommunication = new ConcurrentHashMap<>();
    private final Map<String, Tournament> _runningTournaments = new LinkedHashMap<>();
    private final Map<String, TournamentQueue> _tournamentQueues = new LinkedHashMap<>();
    private final HallChatRoomMediator _hallChat;
    private int _tickCounter = TICK_COUNTER_START;
    private final TournamentService _tournamentService;
    private final GameServer _gameServer;
    private final CardBlueprintLibrary _cardBlueprintLibrary;

    public HallServer(AdminService adminService, FormatLibrary formatLibrary,
                      ChatServer chatServer, LeagueService leagueService, CollectionsManager collectionsManager,
                      TournamentService tournamentService, GameServer gameServer, CardBlueprintLibrary cardLibrary) {
        _tournamentService = tournamentService;
        _formatLibrary = formatLibrary;
        _collectionsManager = collectionsManager;
        tableHolder = new TableHolder(adminService, leagueService);
        _hallChat = new HallChatRoomMediator(adminService, HALL_TIMEOUT_PERIOD);
        chatServer.addChatRoom(_hallChat);
        _gameServer = gameServer;
        _cardBlueprintLibrary = cardLibrary;
    }

    final void hallChanged() {
        for (HallCommunicationChannel commChannel : _playerChannelCommunication.values())
            commChannel.hallChanged();
    }

    private void doAfterStartup() {
        for (Tournament tournament : _tournamentService.getLiveTournaments())
            _runningTournaments.put(tournament.getTournamentId(), tournament);
        createStartupGameTables();
    }

    public final void setShutdown(boolean shutdown, ChatServer chatServer) throws SQLException, IOException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            boolean cancelMessage = _shutdown && !shutdown;
            _shutdown = shutdown;
            if (shutdown) {
                tableHolder.cancelWaitingTables();
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
            return tableHolder.getTableCount();
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
            tableHolder.validatePlayerForLeague(player.getName(), gameSettings);
            final GameTable table = tableHolder.createTable(gameSettings, participant);
            table.createGameIfFull(gameServer, this, leagueService);
            hallChanged();
        }
    }


    private void createStartupGameTables() {

        _hallDataAccessLock.writeLock().lock();
        try {
            // Real basic game

            GameSettings gameSettings = new GameSettings(_formatLibrary.get("debug1e"), false,
                    false, false, GameTimer.DEBUG_TIMER, "Startup Sample Game");

            Map<SubDeck, List<String>> deck1 = new HashMap<>();
            deck1.put(SubDeck.SEED_DECK, List.of("101_104", "109_063", "101_106", "111_009"));
            deck1.put(SubDeck.MISSIONS, List.of("204_003"));
            List<String> drawDeck1 = new ArrayList<>();
            drawDeck1.addAll(List.of("101_215", "101_304", "155_053"));
            drawDeck1.addAll(Collections.nCopies(3, "101_293"));
            drawDeck1.addAll(Collections.nCopies(2, "101_271"));
            drawDeck1.addAll(Collections.nCopies(15, "101_331"));
            drawDeck1.addAll(Collections.nCopies(6, "101_202"));
            drawDeck1.addAll(Collections.nCopies(3, "101_201"));
            drawDeck1.addAll(Collections.nCopies(3, "101_320"));
            deck1.put(SubDeck.DRAW_DECK, drawDeck1);

            Map<SubDeck, List<String>> deck2 = new HashMap<>();
            deck2.put(SubDeck.MISSIONS, List.of("101_148", "181_037", "123_079"));
            deck2.put(SubDeck.SEED_DECK, List.of("101_106", "105_015", "116_011", "115_010"));
            deck2.put(SubDeck.DRAW_DECK, Collections.nCopies(34, "101_320"));

            GameParticipant participant1 = new GameParticipant("asdf", new CardDeck(deck1));
            GameParticipant participant2 = new GameParticipant("qwer", new CardDeck(deck2));

            final GameTable table = tableHolder.createTable(gameSettings, participant1, participant2);
            table.createGameWithNoLeague(_gameServer,this);

            try {

                // Game in the middle of a battle
                GameSettings gameSettings2 = new GameSettings(_formatLibrary.get("debug1e"), false,
                        false, false, GameTimer.DEBUG_TIMER, "Ship Battle Game");
                ST1EGame game = SampleGameBuilder.testShipBattleGame(_formatLibrary, 30, "asdf",
                        "qwer", _cardBlueprintLibrary);
                GameParticipant newParticipant1 = new GameParticipant("asdf", new CardDeck(new HashMap<>()));
                GameParticipant newParticipant2 = new GameParticipant("qwer", new CardDeck(new HashMap<>()));
                final GameTable table2 = tableHolder.createTable(gameSettings2, newParticipant1, newParticipant2);
                table2.createGameWithNoLeague(_gameServer, this);
            } catch(CardNotFoundException | InvalidGameOperationException exp) {
                LOGGER.error(exp);
            }

            hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void createTournamentGameInternal(GameServer gameServer, GameSettings gameSettings,
                                             GameParticipant[] participants,
                                             String tournamentName, GameResultListener listener) {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            if (!_shutdown) {
                final GameTable gameTable = tableHolder.createTable(gameSettings, participants);
                List<GameResultListener> listenerList = List.of(listener,
                        new NotifyHallListenersGameResultListener(this));
                gameTable.createTournamentGameInternal(gameServer, listenerList, tournamentName);
            }
        }
    }

    public final void joinQueue(String queueId, User player, String deckName, CardBlueprintLibrary cardLibrary,
                                DeckDAO deckDAO)
            throws HallException, SQLException, IOException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            TournamentQueue tournamentQueue = _tournamentQueues.get(queueId);
            if (tournamentQueue == null)
                throw new HallException(
                        "Tournament queue already finished accepting players, try again in a few seconds");
            CardDeck cardDeck = null;
            if (tournamentQueue.isRequiresDeck())
                cardDeck = validateUserAndDeck(tournamentQueue.getGameFormat(_formatLibrary), player, deckName,
                        cardLibrary, deckDAO);
            if (tournamentQueue.isPlayerSignedUp(player.getName()))
                throw new HallException("You have already joined that queue");
            tournamentQueue.joinPlayer(_collectionsManager, player, cardDeck);

            hallChanged();
        }
    }

    public final void joinTableAsPlayer(String tableId, User player, User deckOwner, String deckName,
                                        CardBlueprintLibrary cardBlueprintLibrary, DeckDAO deckDAO,
                                        LeagueService leagueService)
            throws HallException {
        if (_shutdown)
            throw new HallException(
                    "Server is in shutdown mode. Server will be restarted after all running games are finished.");

        GameSettings gameSettings = tableHolder.getGameSettings(tableId);
        CardDeck cardDeck = validateUserAndDeck(gameSettings.getGameFormat(), deckOwner, deckName,
                cardBlueprintLibrary, deckDAO);

        try (CloseableWriteLock ignored = _writeLock.open()) {
            tableHolder.joinTable(tableId, player, cardDeck, _gameServer, this, leagueService);
        }
    }


    public final void leaveQueue(String queueId, User player) throws SQLException, IOException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            TournamentQueue tournamentQueue = _tournamentQueues.get(queueId);
            if (tournamentQueue != null && tournamentQueue.isPlayerSignedUp(player.getName())) {
                tournamentQueue.leavePlayer(_collectionsManager, player);
                hallChanged();
            }
        }
    }

    private boolean leaveQueuesForLeavingPlayer(User player) throws SQLException, IOException {
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
            tableHolder.leaveAwaitingTable(player, tableId);
            hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    private boolean leaveAwaitingTablesForLeavingPlayer(User player) {
        _hallDataAccessLock.writeLock().lock();
        try {
            return tableHolder.leaveAwaitingTablesForPlayer(player.getName());
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

            tableHolder.processTables(player, playedGamesOnServer, tablesOnServer);

            for (Map.Entry<String, TournamentQueue> tournamentQueueEntry : _tournamentQueues.entrySet()) {
                String tournamentQueueKey = tournamentQueueEntry.getKey();
                TournamentQueue tournamentQueue = tournamentQueueEntry.getValue();
                GameFormat gameFormat = tournamentQueue.getGameFormat(_formatLibrary);

                Map<String, String> props = new HashMap<>();
                props.put("cost", String.valueOf(tournamentQueue.getCost()));
                props.put("collection", tournamentQueue.getCollectionType().getFullName());
                props.put("format", gameFormat.getName());
                props.put("queueName", tournamentQueue.getTournamentQueueName());
                props.put("playerCount", String.valueOf(tournamentQueue.getPlayerCount()));
                props.put("prizes", tournamentQueue.getPrizesDescription());
                props.put("system", tournamentQueue.getPairingDescription());
                props.put("start", tournamentQueue.getStartCondition());
                props.put("signedUp", String.valueOf(tournamentQueue.isPlayerSignedUp(player.getName())));
                props.put("joinable", String.valueOf(tournamentQueue.isJoinable()));

                tournamentQueuesOnServer.put(tournamentQueueKey, props);
            }

            for (Map.Entry<String, Tournament> tournamentEntry : _runningTournaments.entrySet()) {
                String tournamentKey = tournamentEntry.getKey();
                Tournament tournament = tournamentEntry.getValue();

                Map<String, String> props = new HashMap<>();
                props.put("collection", tournament.getCollectionType().getFullName());
                props.put("format", _formatLibrary.get(tournament.getFormat()).getName());
                props.put("name", tournament.getTournamentName());
                props.put("system", tournament.getPlayOffSystem());
                props.put("stage", tournament.getTournamentStage().getHumanReadable());
                props.put("round", String.valueOf(tournament.getCurrentRound()));
                props.put("playerCount", String.valueOf(tournament.getPlayersInCompetitionCount()));
                props.put("signedUp", String.valueOf(tournament.isPlayerInCompetition(player.getName())));

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
            // Remove finished games
            tableHolder.removeFinishedGames();

            long currentTime = System.currentTimeMillis();
            Map<User, HallCommunicationChannel> visitCopy = new LinkedHashMap<>(_playerChannelCommunication);
            for (Map.Entry<User, HallCommunicationChannel> lastVisitedPlayer : visitCopy.entrySet()) {
                if (currentTime > lastVisitedPlayer.getValue().getLastAccessed() + PLAYER_TABLE_INACTIVITY_PERIOD) {
                    User player = lastVisitedPlayer.getKey();
                    boolean leftTables = leaveAwaitingTablesForLeavingPlayer(player);
                    boolean leftQueues = leaveQueuesForLeavingPlayer(player);
                    if (leftTables || leftQueues)
                        hallChanged();
                }

                if (currentTime > lastVisitedPlayer.getValue().getLastAccessed() + PLAYER_CHAT_INACTIVITY_PERIOD) {
                    User player = lastVisitedPlayer.getKey();
                    _playerChannelCommunication.remove(player);
                }
            }

            TournamentQueueCallback queueCallback;

            for (Map.Entry<String, TournamentQueue> runningTournamentQueue :
                    new HashMap<>(_tournamentQueues).entrySet()) {
                String tournamentQueueKey = runningTournamentQueue.getKey();
                TournamentQueue tournamentQueue = runningTournamentQueue.getValue();
                queueCallback = new HallTournamentQueueCallback(_runningTournaments);
                // If it's finished, remove it
                tournamentQueue.process(queueCallback, _collectionsManager, _tournamentService);
                if (tournamentQueue.shouldBeRemovedFromHall()) {
                    _tournamentQueues.remove(tournamentQueueKey);
                    hallChanged();
                }
            }

            for (Map.Entry<String, Tournament> tournamentEntry : new HashMap<>(_runningTournaments).entrySet()) {
                Tournament runningTournament = tournamentEntry.getValue();
                GameFormat tournamentFormat = _formatLibrary.get(runningTournament.getFormat());
                HallTournamentCallback callback = new HallTournamentCallback(this,
                        _gameServer, runningTournament, tournamentFormat);
                boolean changed = runningTournament.advanceTournament(callback, _collectionsManager);
                if (runningTournament.getTournamentStage() == Tournament.Stage.FINISHED)
                    _runningTournaments.remove(tournamentEntry.getKey());
                if (changed)
                    hallChanged();
            }

            if (_tickCounter == TICK_COUNTER_START) {
                _tickCounter = 0;
                long nextLoadTime = System.currentTimeMillis() + SCHEDULED_TOURNAMENT_LOAD_TIME;
                List<TournamentQueueInfo> futureTournamentQueues =
                        _tournamentService.getFutureScheduledTournamentQueues(nextLoadTime);
                for (TournamentQueueInfo queueInfo : futureTournamentQueues) {
                    String tournamentId = queueInfo.getScheduledTournamentId();
                    if (!_tournamentQueues.containsKey(tournamentId)) {
                        ScheduledTournamentQueue scheduledQueue =
                                queueInfo.createNewScheduledTournamentQueue(
                                        _cardBlueprintLibrary, Tournament.Stage.PLAYING_GAMES, _tournamentService);
                        _tournamentQueues.put(tournamentId, scheduledQueue);
                        hallChanged();
                    }
                }
            }
            _tickCounter++;
        }
    }

    public final void startServer() {
        basicStartup();
        doAfterStartup();
    }

    public void sendAdminMessage(String senderName, String message)
            throws PrivateInformationException, ChatCommandErrorException {
        _hallChat.sendChatMessage(senderName, message, true);
    }

    public boolean isShutdown() {
        return _shutdown;
    }

}