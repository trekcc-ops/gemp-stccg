package com.gempukku.stccg.hall;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.SubscriptionConflictException;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.chat.ChatCommandErrorException;
import com.gempukku.stccg.chat.HallChatRoomMediator;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.database.IgnoreDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.database.UserNotFoundException;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueNotFoundException;
import com.gempukku.stccg.league.LeagueSeriesData;
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
    private static final String DEFAULT_MESSAGE_OF_THE_DAY = "Lorem ipsum dolor sit amet, " +
            "consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    private static final long SCHEDULED_TOURNAMENT_LOAD_TIME = 1000 * 60 * 60 * 24 * 7; // 1 week
    private final FormatLibrary _formatLibrary;
    private final CollectionsManager _collectionsManager;
    private String _messageOfTheDay;
    private boolean _shutdown;
    private final ReadWriteLock _hallDataAccessLock = new ReentrantReadWriteLock(false);
    private final TableHolder tableHolder;
    private final Map<User, HallCommunicationChannel> _playerChannelCommunication = new ConcurrentHashMap<>();
    private int _nextChannelNumber = 0;
    private final Map<String, Tournament> _runningTournaments = new LinkedHashMap<>();
    private final Map<String, TournamentQueue> _tournamentQueues = new LinkedHashMap<>();
    private final HallChatRoomMediator _hallChat;
    private final ServerObjects _serverObjects;
    private int _tickCounter = TICK_COUNTER_START;

    public HallServer(ServerObjects objects) {
        _serverObjects = objects;
        _formatLibrary = objects.getFormatLibrary();
        _collectionsManager = objects.getCollectionsManager();
        final IgnoreDAO ignoreDAO = objects.getIgnoreDAO();
        tableHolder = new TableHolder(objects);
        _hallChat = new HallChatRoomMediator(_serverObjects, HALL_TIMEOUT_PERIOD);
        _messageOfTheDay = DEFAULT_MESSAGE_OF_THE_DAY;
        objects.getChatServer().addChatRoom(_hallChat);
    }

    final void hallChanged() {
        for (HallCommunicationChannel commChannel : _playerChannelCommunication.values())
            commChannel.hallChanged();
    }

    private void doAfterStartup() {
        for (Tournament tournament : _serverObjects.getTournamentService().getLiveTournaments())
            _runningTournaments.put(tournament.getTournamentId(), tournament);
        createStartupGameTable();
    }

    public final void setShutdown(boolean shutdown) throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
            boolean cancelMessage = _shutdown && !shutdown;
            _shutdown = shutdown;
            if (shutdown) {
                tableHolder.cancelWaitingTables();
                cancelTournamentQueues();
                _serverObjects.getChatServer().sendSystemMessageToAllUsers(
                        "System is entering shutdown mode and will be restarted when all games are finished.");
                hallChanged();
            }
            else if(cancelMessage){
                _serverObjects.getChatServer().sendSystemMessageToAllUsers(
                        "Shutdown mode canceled; games may now resume.");
            }
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public final String getDailyMessage() {
        _hallDataAccessLock.readLock().lock();
        try {
            return _messageOfTheDay;
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    public final void setDailyMessage(String message) {
        _hallDataAccessLock.writeLock().lock();
        try {
            _messageOfTheDay = message;
            hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public DefaultGame getGameById(String gameId) throws HttpProcessingException {
        GameServer gameServer = _serverObjects.getGameServer();
        CardGameMediator mediator = gameServer.getGameById(gameId);
        return mediator.getGame();
    }

    public final int getTablesCount() {
        _hallDataAccessLock.readLock().lock();
        try {
            return tableHolder.getTableCount();
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    private void cancelTournamentQueues() throws SQLException, IOException {
        for (TournamentQueue tournamentQueue : _tournamentQueues.values())
            tournamentQueue.leaveAllPlayers(_collectionsManager);
    }

    public final void createNewTable(String type, User player, User deckOwner, String deckName, GameTimer timer,
                                     String description, boolean isInviteOnly, boolean isPrivate, boolean isHidden)
            throws HallException {
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. " +
                    "Server will be restarted after all running games are finished.");
        GameSettings gameSettings = createGameSettings(type, timer, description, isInviteOnly, isPrivate, isHidden);
        CardDeck cardDeck = validateUserAndDeck(gameSettings.getGameFormat(), deckOwner, deckName);

        _hallDataAccessLock.writeLock().lock();
        try {
            GameParticipant participant = new GameParticipant(player, cardDeck);
            tableHolder.validatePlayerForLeague(player.getName(), gameSettings);
            final GameTable table = tableHolder.createTable(gameSettings, participant);
            if (table.isFull())
                createGameFromTable(table);

            hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    private void createStartupGameTable() {

        _hallDataAccessLock.writeLock().lock();
        try {
            GameSettings gameSettings = createGameSettings("debug1e", GameTimer.DEBUG_TIMER,
                    "Startup Sample Game", false, false, false);

            User player1 = _serverObjects.getPlayerDAO().getPlayer("asdf");
            User player2 = _serverObjects.getPlayerDAO().getPlayer("qwer");

            CardDeck cardDeck1 = validateUserAndDeck(gameSettings.getGameFormat(), player1, "AMS Deck");
            CardDeck cardDeck2 = validateUserAndDeck(gameSettings.getGameFormat(), player2, "Rommie Test");

            GameParticipant participant1 = new GameParticipant("asdf", cardDeck1);
            GameParticipant participant2 = new GameParticipant("qwer", cardDeck2);

            final GameTable table = tableHolder.createTable(gameSettings, participant1, participant2);
            createGameFromTable(table);
            hallChanged();
        } catch (UserNotFoundException | HallException exp) {
            LOGGER.error(exp);
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void createTournamentGameInternal(GameSettings gameSettings, GameParticipant[] participants,
                                             String tournamentName, GameResultListener listener) {
        _hallDataAccessLock.writeLock().lock();
        try {
            if (!_shutdown) {
                final GameTable gameTable = tableHolder.createTable(gameSettings, participants);
                createGameMediator(participants, tournamentName, gameTable, listener);
            }
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    private GameSettings createGameSettings(String formatSelection, GameTimer timer, String description,
                                            boolean isInviteOnly, boolean isPrivate, boolean isHidden)
            throws HallException {
        League league = null;
        LeagueSeriesData seriesData = null;
        GameFormat format = _formatLibrary.getHallFormats().get(formatSelection);
        GameTimer gameTimer = timer;

        if (format == null) {
            // Maybe it's a league format?
            try {
                league = _serverObjects.getLeagueService().getLeagueByType(formatSelection);
                seriesData = _serverObjects.getLeagueService().getCurrentLeagueSeries(league);
                if (seriesData == null)
                    throw new HallException("There is no ongoing series for that league");

                if (isInviteOnly) {
                    throw new HallException("League games cannot be invite-only");
                }

                if (isPrivate) {
                    throw new HallException("League games cannot be private");
                }

                //Don't want people getting around the anonymity for leagues.
                if (description != null)
                    description = "";

                format = seriesData.getFormat();

                gameTimer = GameTimer.COMPETITIVE_TIMER;
            } catch(LeagueNotFoundException ignored) {

            }
        }
        // It's not a normal format and also not a league one
        if (format == null)
            throw new HallException("This format is not supported: " + formatSelection);

        return new GameSettings(format, league, seriesData,
                league != null, isPrivate, isInviteOnly, isHidden, gameTimer, description);
    }

    public final void joinQueue(String queueId, User player, String deckName)
            throws HallException, SQLException, IOException {
        if (_shutdown)
            throw new HallException(
                    "Server is in shutdown mode. Server will be restarted after all running games are finished.");

        _hallDataAccessLock.writeLock().lock();
        try {
            TournamentQueue tournamentQueue = _tournamentQueues.get(queueId);
            if (tournamentQueue == null)
                throw new HallException(
                        "Tournament queue already finished accepting players, try again in a few seconds");
            if (tournamentQueue.isPlayerSignedUp(player.getName()))
                throw new HallException("You have already joined that queue");

            CardDeck cardDeck = null;
            if (tournamentQueue.isRequiresDeck())
                cardDeck = validateUserAndDeck(_formatLibrary.get(tournamentQueue.getFormat()), player, deckName);

            tournamentQueue.joinPlayer(_collectionsManager, player, cardDeck);

            hallChanged();

        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    /**
     *
     */
    public final void joinTableAsPlayer(String tableId, User player, String deckName) throws HallException {
        LOGGER.debug("HallServer - joinTableAsPlayer function called");
        if (_shutdown)
            throw new HallException(
                    "Server is in shutdown mode. Server will be restarted after all running games are finished.");

        GameSettings gameSettings = tableHolder.getGameSettings(tableId);
        CardDeck cardDeck = validateUserAndDeck(gameSettings.getGameFormat(), player, deckName);

        _hallDataAccessLock.writeLock().lock();
        try {
            final GameTable runningTable = tableHolder.joinTable(tableId, player, cardDeck);
            if (runningTable.isFull())
                createGameFromTable(runningTable);

            hallChanged();

        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public final void joinTableAsPlayerWithSpoofedDeck(String tableId, User player, User librarian, String deckName)
            throws HallException {
        if (_shutdown)
            throw new HallException(
                    "Server is in shutdown mode. Server will be restarted after all running games are finished.");

        GameSettings gameSettings = tableHolder.getGameSettings(tableId);
        CardDeck cardDeck = validateUserAndDeck(gameSettings.getGameFormat(), librarian, deckName);

        _hallDataAccessLock.writeLock().lock();
        try {
            final GameTable runningTable = tableHolder.joinTable(tableId, player, cardDeck);
            if (runningTable.isFull())
                createGameFromTable(runningTable);

            hallChanged();

        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public final void leaveQueue(String queueId, User player) throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
            TournamentQueue tournamentQueue = _tournamentQueues.get(queueId);
            if (tournamentQueue != null && tournamentQueue.isPlayerSignedUp(player.getName())) {
                tournamentQueue.leavePlayer(_collectionsManager, player);
                hallChanged();
            }
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    private boolean leaveQueuesForLeavingPlayer(User player) throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
            boolean result = false;
            for (TournamentQueue tournamentQueue : _tournamentQueues.values()) {
                if (tournamentQueue.isPlayerSignedUp(player.getName())) {
                    tournamentQueue.leavePlayer(_collectionsManager, player);
                    result = true;
                }
            }
            return result;
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public final void dropFromTournament(String tournamentId, User player) {
        _hallDataAccessLock.writeLock().lock();
        try {
            Tournament tournament = _runningTournaments.get(tournamentId);
            if (tournament != null) {
                tournament.dropPlayer(player.getName());
                hallChanged();
            }
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public final void leaveAwaitingTable(User player, String tableId) {
        _hallDataAccessLock.writeLock().lock();
        try {
            if (tableHolder.leaveAwaitingTable(player, tableId))
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
        _hallDataAccessLock.readLock().lock();
        try {
            HallCommunicationChannel channel = new HallCommunicationChannel(_nextChannelNumber++);
            _playerChannelCommunication.put(player, channel);
            return channel;
        } finally {
            _hallDataAccessLock.readLock().unlock();
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
                           Set<String> playedGamesOnServer, Map<String, Map<String, String>> tablesOnServer,
                           Map<String, Map<String, String>> tournamentsOnServer, Map<Object, Object> itemsToSerialize) {
        _hallDataAccessLock.readLock().lock();
        try {
            String currentTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            itemsToSerialize.put("serverTime", currentTime);
            if (_messageOfTheDay != null)
                itemsToSerialize.put("messageOfTheDay", _messageOfTheDay);

            tableHolder.processTables(player, playedGamesOnServer, tablesOnServer);

            for (Map.Entry<String, TournamentQueue> tournamentQueueEntry : _tournamentQueues.entrySet()) {
                String tournamentQueueKey = tournamentQueueEntry.getKey();
                TournamentQueue tournamentQueue = tournamentQueueEntry.getValue();
                GameFormat gameFormat = _formatLibrary.get(tournamentQueue.getFormat());

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
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    private CardDeck validateUserAndDeck(GameFormat format, User player, String deckName) throws HallException {
        CardDeck cardDeck = _serverObjects.getDeckDAO().getDeckForUser(player, deckName);
        if (cardDeck == null)
            throw new HallException("You don't have a deck registered yet");

        CardDeck deck = format.applyErrata(_serverObjects.getCardBlueprintLibrary(), cardDeck);
        List<String> validations = format.validateDeck(_serverObjects.getCardBlueprintLibrary(), deck);
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


    private void createGameFromTable(GameTable gameTable) {
        Set<GameParticipant> players = gameTable.getPlayers();
        GameParticipant[] participants = players.toArray(new GameParticipant[0]);
        final League league = gameTable.getGameSettings().getLeague();
        final LeagueSeriesData seriesData = gameTable.getGameSettings().getSeriesData();

        String tournamentName = (league != null) ?
                (league.getName() + " - " + seriesData.getName()) :
                ("Casual - " + gameTable.getGameSettings().getTimeSettings().name());

        if (league == null) {
            createGameMediator(participants, tournamentName, gameTable);
        } else {
            GameResultListener listener =
                    new LeagueGameResultListener(league, seriesData, _serverObjects.getLeagueService());
            createGameMediator(participants, tournamentName, gameTable, listener);
        }
    }

    private void createGameMediator(GameParticipant[] participants, String tournamentName,
                                    GameTable gameTable, GameResultListener... listeners) {
        final CardGameMediator cardGameMediator =
                _serverObjects.getGameServer().createNewGame(tournamentName, participants, gameTable.getGameSettings(),
                        _serverObjects.getCardBlueprintLibrary());

        for (GameResultListener listener : listeners) {
            cardGameMediator.addGameResultListener(listener);
        }
        cardGameMediator.startGame();
        cardGameMediator.addGameResultListener(new NotifyHallListenersGameResultListener(this));
        gameTable.startGame(cardGameMediator);
    }

    @Override
    protected final void cleanup() throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
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
                if (tournamentQueue.process(queueCallback, _collectionsManager)) {
                    _tournamentQueues.remove(tournamentQueueKey);
                    hallChanged();
                }
            }

            for (Map.Entry<String, Tournament> tournamentEntry : new HashMap<>(_runningTournaments).entrySet()) {
                Tournament runningTournament = tournamentEntry.getValue();
                GameFormat tournamentFormat = _formatLibrary.get(runningTournament.getFormat());
                boolean changed = runningTournament.advanceTournament(
                        new HallTournamentCallback(this, runningTournament, tournamentFormat), _collectionsManager);
                if (runningTournament.getTournamentStage() == Tournament.Stage.FINISHED)
                    _runningTournaments.remove(tournamentEntry.getKey());
                if (changed)
                    hallChanged();
            }

            if (_tickCounter == TICK_COUNTER_START) {
                _tickCounter = 0;
                long nextLoadTime = System.currentTimeMillis() + SCHEDULED_TOURNAMENT_LOAD_TIME;
                List<TournamentQueueInfo> futureTournamentQueues =
                        _serverObjects.getTournamentService().getFutureScheduledTournamentQueues(nextLoadTime);
                for (TournamentQueueInfo queueInfo : futureTournamentQueues) {
                    String tournamentId = queueInfo.getScheduledTournamentId();
                    if (!_tournamentQueues.containsKey(tournamentId)) {
                        ScheduledTournamentQueue scheduledQueue =
                                queueInfo.createNewScheduledTournamentQueue(
                                        _serverObjects, Tournament.Stage.PLAYING_GAMES);
                        _tournamentQueues.put(tournamentId, scheduledQueue);
                        hallChanged();
                    }
                }
            }
            _tickCounter++;

        } finally {
            _hallDataAccessLock.writeLock().unlock();
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
}