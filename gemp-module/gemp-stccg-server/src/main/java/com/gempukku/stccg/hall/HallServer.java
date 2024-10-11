package com.gempukku.stccg.hall;

import com.gempukku.stccg.*;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.chat.ChatCommandErrorException;
import com.gempukku.stccg.chat.ChatRoomMediator;
import com.gempukku.stccg.chat.ChatServer;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameFormat;
import com.gempukku.stccg.db.IgnoreDAO;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.db.vo.League;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameParticipant;
import com.gempukku.stccg.game.GameResultListener;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.service.AdminService;
import com.gempukku.stccg.tournament.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class HallServer extends AbstractServer {

    private static final Logger LOGGER = LogManager.getLogger(HallServer.class);

    private static final int _playerTableInactivityPeriod = 1000 * 20 ; // 20 seconds

    private static final int _playerChatInactivityPeriod = 1000 * 60 * 5; // 5 minutes
    private static final long _scheduledTournamentLoadTime = 1000 * 60 * 60 * 24 * 7; // Week
    // Repeat tournaments every 2 days

    private final ChatServer _chatServer;
    private final LeagueService _leagueService;
    private final TournamentService _tournamentService;
    private final FormatLibrary _formatLibrary;
    private final CollectionsManager _collectionsManager;
    private final GameServer _gameServer;
    private final AdminService _adminService;

    private String _messageOfTheDay;

    private boolean _shutdown;

    private final ReadWriteLock _hallDataAccessLock = new ReentrantReadWriteLock(false);
    
    private final TableHolder tableHolder;
    
    private final Map<User, HallCommunicationChannel> _playerChannelCommunication = new ConcurrentHashMap<>();
    private int _nextChannelNumber = 0;
    
    private final Map<String, Tournament> _runningTournaments = new LinkedHashMap<>();

    private final Map<String, TournamentQueue> _tournamentQueues = new LinkedHashMap<>();
    private final ChatRoomMediator _hallChat;
    private final GameResultListener _notifyHallListeners = new NotifyHallListenersGameResultListener();
    private final ServerObjects _serverObjects;

    public HallServer(ServerObjects objects) {
        _serverObjects = objects;
        _gameServer = objects.getGameServer();
        _chatServer = objects.getChatServer();
        _leagueService = objects.getLeagueService();
        _tournamentService = objects.getTournamentService();
        _formatLibrary = objects.getFormatLibrary();
        _collectionsManager = objects.getCollectionsManager();
        _adminService = objects.getAdminService();
        final IgnoreDAO ignoreDAO = objects.getIgnoreDAO();

        tableHolder = new TableHolder(_leagueService, ignoreDAO);
        
        _hallChat = _chatServer.createChatRoom(
                "Game Hall", true, 300, true,
                "You're now in the Game Hall, use /help to get a list of available commands.<br>" +
                        "Don't forget to check out the new Discord chat integration! " +
                        "Click the 'Switch to Discord' button in the lower right ---->"
        );
        _hallChat.addChatCommandCallback("ban",
                (from, parameters, admin) -> {
                    if (admin) {
                        _adminService.banUser(parameters.trim());
                    } else {
                        throw new ChatCommandErrorException("Only administrator can ban users");
                    }
                });
        _hallChat.addChatCommandCallback("banIp",
                (from, parameters, admin) -> {
                    if (admin) {
                        _adminService.banIp(parameters.trim());
                    } else {
                        throw new ChatCommandErrorException("Only administrator can ban users");
                    }
                });
        _hallChat.addChatCommandCallback("banIpRange",
                (from, parameters, admin) -> {
                    if (admin) {
                        _adminService.banIpPrefix(parameters.trim());
                    } else {
                        throw new ChatCommandErrorException("Only administrator can ban users");
                    }
                });
        _hallChat.addChatCommandCallback("ignore",
                (from, parameters, admin) -> {
                    final String playerName = parameters.trim();
                    if (playerName.length() >= 2 && playerName.length() <= 30) {
                        if (!from.equals(playerName) && ignoreDAO.addIgnoredUser(from, playerName)) {
                            _hallChat.sendToUser("System", from, "User " + playerName + " added to ignore list");
                        } else if (from.equals(playerName)) {
                            for (String message : getIgnoredUserMessages())
                                _hallChat.sendToUser(from, from, message);
                        } else {
                            _hallChat.sendToUser("System", from, "User " + playerName + " is already on your ignore list");
                        }
                    } else {
                        _hallChat.sendToUser("System", from, playerName + " is not a valid username");
                    }
                });
        _hallChat.addChatCommandCallback("unignore",
                (from, parameters, admin) -> {
                    final String playerName = parameters.trim();
                    if (playerName.length() >= 2 && playerName.length() <= 10) {
                        if (ignoreDAO.removeIgnoredUser(from, playerName)) {
                            _hallChat.sendToUser("System", from, "User " + playerName + " removed from ignore list");
                        } else {
                            _hallChat.sendToUser("System", from, "User " + playerName + " wasn't on your ignore list. Try ignoring them first.");
                        }
                    } else {
                        _hallChat.sendToUser("System", from, playerName + " is not a valid username");
                    }
                });
        _hallChat.addChatCommandCallback("listIgnores",
                (from, parameters, admin) -> {
                    final Set<String> ignoredUsers = ignoreDAO.getIgnoredUsers(from);
                    _hallChat.sendToUser("System", from, "Your ignores: " + Arrays.toString(ignoredUsers.toArray(new String[0])));
                });
        _hallChat.addChatCommandCallback("incognito",
                (from, parameters, admin) -> {
                    _hallChat.setIncognito(from, true);
                    _hallChat.sendToUser("System", from, "You are now incognito (do not appear in user list)");
                });
        _hallChat.addChatCommandCallback("endIncognito",
                (from, parameters, admin) -> {
                    _hallChat.setIncognito(from, false);
                    _hallChat.sendToUser("System", from, "You are no longer incognito");
                });
        _hallChat.addChatCommandCallback("help",
                (from, parameters, admin) -> {
                    //_hallChat.sendToUser("System", from,
                    String message = """
                    List of available commands:
                    /ignore username - Adds user 'username' to list of your ignores
                    /unignore username - Removes user 'username' from list of your ignores
                    /listIgnores - Lists all your ignored users
                    /incognito - Makes you incognito (not visible in user list)
                    /endIncognito - Turns your visibility 'on' again""";
                    if (admin) {
                        message += """
                        
                        
                        Admin only commands:
                        /ban username - Bans user 'username' permanently
                        /banIp ip - Bans specified ip permanently
                        /banIpRange ip - Bans ips with the specified prefix, ie. 10.10.10.""";
                    }

                    _hallChat.sendToUser("System", from, message.replace("\n", "<br />"));
                });
        _hallChat.addChatCommandCallback("noCommand",
                (from, parameters, admin) -> _hallChat.sendToUser(
                        "System", from, "\"" + parameters + "\" is not a recognized command."
                ));
    }

    private void hallChanged() {
        for (HallCommunicationChannel hallCommunicationChannel : _playerChannelCommunication.values())
            hallCommunicationChannel.hallChanged();
    }

    @Override
    protected void doAfterStartup() {
        for (Tournament tournament : _tournamentService.getLiveTournaments())
            _runningTournaments.put(tournament.getTournamentId(), tournament);
    }

    public void setShutdown(boolean shutdown) throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
            boolean cancelMessage = _shutdown && !shutdown;
            _shutdown = shutdown;
            if (shutdown) {
                tableHolder.cancelWaitingTables();
                cancelTournamentQueues();
                _chatServer.sendSystemMessageToAllUsers(
                        "System is entering shutdown mode and will be restarted when all games are finished.");
                hallChanged();
            }
            else if(cancelMessage){
                _chatServer.sendSystemMessageToAllUsers(
                        "Shutdown mode canceled; games may now resume.");
            }
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public String getDailyMessage() {
        _hallDataAccessLock.readLock().lock();
        try {
            return _messageOfTheDay;
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    public void setDailyMessage(String message) {
        _hallDataAccessLock.writeLock().lock();
        try {
            _messageOfTheDay = message;
            hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public int getTablesCount() {
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

    public void createNewTable(String format, User player, String deckName, String timer,
                               String description, boolean isInviteOnly, boolean isPrivate, boolean isHidden)
            throws HallException {
        createNewTable(format, player, player, deckName, timer, description, isInviteOnly, isPrivate, isHidden);
    }

    public void createNewTable(String type, User player, User deckOwner, String deckName, String timer,
                               String description, boolean isInviteOnly, boolean isPrivate, boolean isHidden)
            throws HallException {
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. " +
                    "Server will be restarted after all running games are finished.");
        GameSettings gameSettings = createGameSettings(type, timer, description, isInviteOnly, isPrivate, isHidden);
        CardDeck cardDeck = validateUserAndDeck(gameSettings.getGameFormat(), deckOwner, deckName);

        _hallDataAccessLock.writeLock().lock();
        try {
            final GameTable table = tableHolder.createTable(player, gameSettings, cardDeck);
            if (table != null)
                createGameFromTable(table);

            hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    private GameSettings createGameSettings(String formatSelection, String timer, String description,
                                            boolean isInviteOnly, boolean isPrivate, boolean isHidden)
            throws HallException {
        League league = null;
        LeagueSeriesData seriesData = null;
        GameFormat format = _formatLibrary.getHallFormats().get(formatSelection);
        GameTimer gameTimer = GameTimer.ResolveTimer(timer);

        if (format == null) {
            // Maybe it's a league format?
            league = _leagueService.getLeagueByType(formatSelection);
            if (league != null) {
                seriesData = _leagueService.getCurrentLeagueSeries(league);
                if (seriesData == null)
                    throw new HallException("There is no ongoing series for that league");

                if(isInviteOnly) {
                    throw new HallException("League games cannot be invite-only");
                }

                if(isPrivate) {
                    throw new HallException("League games cannot be private");
                }

                //Don't want people getting around the anonymity for leagues.
                if(description != null)
                    description = "";

                format = seriesData.getFormat();

                gameTimer = GameTimer.COMPETITIVE_TIMER;
            }
        }
        // It's not a normal format and also not a league one
        if (format == null)
            throw new HallException("This format is not supported: " + formatSelection);

        return new GameSettings(format, league, seriesData,
                league != null, isPrivate, isInviteOnly, isHidden, gameTimer, description);
    }

    public void joinQueue(String queueId, User player, String deckName) throws HallException, SQLException, IOException {
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. Server will be restarted after all running games are finished.");

        _hallDataAccessLock.writeLock().lock();
        try {
            TournamentQueue tournamentQueue = _tournamentQueues.get(queueId);
            if (tournamentQueue == null)
                throw new HallException("Tournament queue already finished accepting players, try again in a few seconds");
            if (tournamentQueue.isPlayerSignedUp(player.getName()))
                throw new HallException("You have already joined that queue");

            CardDeck cardDeck = null;
            if (tournamentQueue.isRequiresDeck())
                cardDeck = validateUserAndDeck(_formatLibrary.getFormat(tournamentQueue.getFormat()), player, deckName);

            tournamentQueue.joinPlayer(_collectionsManager, player, cardDeck);

            hallChanged();

        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    /**
     *
     */
    public void joinTableAsPlayer(String tableId, User player, String deckName) throws HallException {
        LOGGER.debug("HallServer - joinTableAsPlayer function called");
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. Server will be restarted after all running games are finished.");

        GameSettings gameSettings = tableHolder.getGameSettings(tableId);
        CardDeck cardDeck = validateUserAndDeck(gameSettings.getGameFormat(), player, deckName);

        _hallDataAccessLock.writeLock().lock();
        try {
            final GameTable runningTable = tableHolder.joinTable(tableId, player, cardDeck);
            if (runningTable != null)
                createGameFromTable(runningTable);

            hallChanged();

        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void joinTableAsPlayerWithSpoofedDeck(String tableId, User player, User librarian, String deckName) throws HallException {
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. Server will be restarted after all running games are finished.");

        GameSettings gameSettings = tableHolder.getGameSettings(tableId);
        CardDeck cardDeck = validateUserAndDeck(gameSettings.getGameFormat(), librarian, deckName);

        _hallDataAccessLock.writeLock().lock();
        try {
            final GameTable runningTable = tableHolder.joinTable(tableId, player, cardDeck);
            if (runningTable != null)
                createGameFromTable(runningTable);

            hallChanged();

        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void leaveQueue(String queueId, User player) throws SQLException, IOException {
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

    public void dropFromTournament(String tournamentId, User player) {
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

    public void leaveAwaitingTable(User player, String tableId) {
        _hallDataAccessLock.writeLock().lock();
        try {
            if (tableHolder.leaveAwaitingTable(player, tableId))
                hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public boolean leaveAwaitingTablesForLeavingPlayer(User player) {
        _hallDataAccessLock.writeLock().lock();
        try {
            return tableHolder.leaveAwaitingTablesForPlayer(player);
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void signupUserForHall(User player, HallChannelVisitor hallChannelVisitor) {
        _hallDataAccessLock.readLock().lock();
        try {
            HallCommunicationChannel channel = new HallCommunicationChannel(_nextChannelNumber++);
            channel.processCommunicationChannel(this, player, hallChannelVisitor);
            _playerChannelCommunication.put(player, channel);
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    public HallCommunicationChannel getCommunicationChannel(User player, int channelNumber)
            throws SubscriptionExpiredException, SubscriptionConflictException {
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

    protected void processHall(User player, HallInfoVisitor visitor) {
        final boolean isAdmin = player.getType().contains("a");
        _hallDataAccessLock.readLock().lock();
        try {
            visitor.serverTime(DateUtils.getCurrentDateAsString());
            if (_messageOfTheDay != null)
                visitor.setDailyMessage(_messageOfTheDay);

            tableHolder.processTables(isAdmin, player, visitor);

            for (Map.Entry<String, TournamentQueue> tournamentQueueEntry : _tournamentQueues.entrySet()) {
                String tournamentQueueKey = tournamentQueueEntry.getKey();
                TournamentQueue tournamentQueue = tournamentQueueEntry.getValue();
                visitor.visitTournamentQueue(tournamentQueueKey, tournamentQueue.getCost(), tournamentQueue.getCollectionType().getFullName(),
                        _formatLibrary.getFormat(tournamentQueue.getFormat()).getName(), tournamentQueue.getTournamentQueueName(),
                        tournamentQueue.getPrizesDescription(), tournamentQueue.getPairingDescription(), tournamentQueue.getStartCondition(),
                        tournamentQueue.getPlayerCount(), tournamentQueue.isPlayerSignedUp(player.getName()), tournamentQueue.isJoinable());
            }

            for (Map.Entry<String, Tournament> tournamentEntry : _runningTournaments.entrySet()) {
                String tournamentKey = tournamentEntry.getKey();
                Tournament tournament = tournamentEntry.getValue();
                visitor.visitTournament(tournamentKey, tournament.getCollectionType().getFullName(),
                        _formatLibrary.getFormat(tournament.getFormat()).getName(), tournament.getTournamentName(), tournament.getPlayOffSystem(),
                        tournament.getTournamentStage().getHumanReadable(),
                        tournament.getCurrentRound(), tournament.getPlayersInCompetitionCount(), tournament.isPlayerInCompetition(player.getName()));
            }
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    private CardDeck validateUserAndDeck(GameFormat format, User player, String deckName) throws HallException {
        CardDeck cardDeck = _gameServer.getParticipantDeck(player, deckName);
        if (cardDeck == null)
            throw new HallException("You don't have a deck registered yet");
        String validation = format.validateDeckForHall(format.applyErrata(cardDeck));
        if(!validation.isEmpty())
            throw new HallException("Your selected deck is not valid for this format: " + validation);
        return cardDeck;
    }


    private String getTournamentName(GameTable table) {
        final League league = table.getGameSettings().getLeague();
        if (league != null)
            return league.getName() + " - " + table.getGameSettings().getSeriesData().getName();
        else
            return "Casual - " + table.getGameSettings().getTimeSettings().name();
    }

    private void createGameFromTable(GameTable gameTable) {
        Set<GameParticipant> players = gameTable.getPlayers();
        GameParticipant[] participants = players.toArray(new GameParticipant[0]);
        final League league = gameTable.getGameSettings().getLeague();
        final LeagueSeriesData seriesData = gameTable.getGameSettings().getSeriesData();

        GameResultListener listener = getGameResultListener(league, seriesData);

        CardGameMediator mediator = createGameMediator(participants, listener, getTournamentName(gameTable), gameTable.getGameSettings());
        gameTable.startGame(mediator);
    }

    private GameResultListener getGameResultListener(League league, LeagueSeriesData seriesData) {
        GameResultListener listener = null;
        if (league != null) {
            listener = new GameResultListener() {
                @Override
                public void gameFinished(String winnerPlayerId, String winReason,
                                         Map<String, String> loserPlayerIdsWithReasons) {
                    _leagueService.reportLeagueGameResult(
                            league, seriesData, winnerPlayerId, loserPlayerIdsWithReasons.keySet().iterator().next());
                }

                @Override
                public void gameCancelled() {
                    // Do nothing...
                }
            };
        }
        return listener;
    }

    private CardGameMediator createGameMediator(GameParticipant[] participants, GameResultListener listener,
                                                String tournamentName, GameSettings gameSettings) {
        final CardGameMediator cardGameMediator = _gameServer.createNewGame(tournamentName, participants, gameSettings);
        if (listener != null)
            cardGameMediator.addGameResultListener(listener);
        cardGameMediator.startGame();
        cardGameMediator.addGameResultListener(_notifyHallListeners);

        return cardGameMediator;
    }

    private class NotifyHallListenersGameResultListener implements GameResultListener {
        @Override
        public void gameCancelled() {
            hallChanged();
        }

        @Override
        public void gameFinished(String winnerPlayerId, String winReason,
                                 Map<String, String> loserPlayerIdsWithReasons) {
            hallChanged();
        }
    }

    private int _tickCounter = 60;

    @Override
    protected void cleanup() throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
            // Remove finished games
            tableHolder.removeFinishedGames();

            long currentTime = System.currentTimeMillis();
            Map<User, HallCommunicationChannel> visitCopy = new LinkedHashMap<>(_playerChannelCommunication);
            for (Map.Entry<User, HallCommunicationChannel> lastVisitedPlayer : visitCopy.entrySet()) {
                if (currentTime > lastVisitedPlayer.getValue().getLastAccessed() + _playerTableInactivityPeriod) {
                    User player = lastVisitedPlayer.getKey();
                    boolean leftTables = leaveAwaitingTablesForLeavingPlayer(player);
                    boolean leftQueues = leaveQueuesForLeavingPlayer(player);
                    if (leftTables || leftQueues)
                        hallChanged();
                }

                if (currentTime > lastVisitedPlayer.getValue().getLastAccessed() + _playerChatInactivityPeriod) {
                    User player = lastVisitedPlayer.getKey();
                    _playerChannelCommunication.remove(player);
                }
            }

            for (Map.Entry<String, TournamentQueue> runningTournamentQueue : new HashMap<>(_tournamentQueues).entrySet()) {
                String tournamentQueueKey = runningTournamentQueue.getKey();
                TournamentQueue tournamentQueue = runningTournamentQueue.getValue();
                HallTournamentQueueCallback queueCallback = new HallTournamentQueueCallback();
                // If it's finished, remove it
                if (tournamentQueue.process(queueCallback, _collectionsManager)) {
                    _tournamentQueues.remove(tournamentQueueKey);
                    hallChanged();
                }
            }

            for (Map.Entry<String, Tournament> tournamentEntry : new HashMap<>(_runningTournaments).entrySet()) {
                Tournament runningTournament = tournamentEntry.getValue();
                boolean changed = runningTournament.advanceTournament(
                        new HallTournamentCallback(runningTournament), _collectionsManager);
                if (runningTournament.getTournamentStage() == Tournament.Stage.FINISHED)
                    _runningTournaments.remove(tournamentEntry.getKey());
                if (changed)
                    hallChanged();
            }

            if (_tickCounter == 60) {
                _tickCounter = 0;
                List<TournamentQueueInfo> futureTournamentQueues = _tournamentService.getFutureScheduledTournamentQueues(
                        System.currentTimeMillis() + _scheduledTournamentLoadTime);
                for (TournamentQueueInfo queueInfo : futureTournamentQueues) {
                    String scheduledTournamentId = queueInfo.getScheduledTournamentId();
                    if (!_tournamentQueues.containsKey(scheduledTournamentId)) {
                        ScheduledTournamentQueue scheduledQueue = new ScheduledTournamentQueue(scheduledTournamentId,
                                Tournament.Stage.PLAYING_GAMES, queueInfo, _serverObjects);
                        _tournamentQueues.put(scheduledTournamentId, scheduledQueue);
                        hallChanged();
                    }
                }
            }
            _tickCounter++;

        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    private class HallTournamentQueueCallback implements TournamentQueueCallback {
        @Override
        public void createTournament(Tournament tournament) {
            _runningTournaments.put(tournament.getTournamentId(), tournament);
        }
    }

    private class HallTournamentCallback implements TournamentCallback {
        private final Tournament _tournament;
        private final GameSettings tournamentGameSettings;

        private HallTournamentCallback(Tournament tournament) {
            _tournament = tournament;
            tournamentGameSettings = new GameSettings(_formatLibrary.getFormat(_tournament.getFormat()),
                    null, null, true, false, false, false,
                    GameTimer.TOURNAMENT_TIMER, null);
        }

        @Override
        public void createGame(String playerOne, CardDeck deckOne, String playerTwo, CardDeck deckTwo) {
            final GameParticipant[] participants = new GameParticipant[2];
            participants[0] = new GameParticipant(playerOne, deckOne);
            participants[1] = new GameParticipant(playerTwo, deckTwo);
            createGameInternal(participants);
        }

        private void createGameInternal(final GameParticipant[] participants) {
            _hallDataAccessLock.writeLock().lock();
            try {
                if (!_shutdown) {
                    final GameTable gameTable = tableHolder.setupTournamentTable(tournamentGameSettings, participants);
                    final CardGameMediator mediator = createGameMediator(participants,
                            new GameResultListener() {
                                @Override
                                public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                                    _tournament.reportGameFinished(winnerPlayerId, loserPlayerIdsWithReasons.keySet().iterator().next());
                                }

                                @Override
                                public void gameCancelled() {
                                    createGameInternal(participants);
                                }
                            }, _tournament.getTournamentName(), tournamentGameSettings);
                    gameTable.startGame(mediator);
                }
            } finally {
                _hallDataAccessLock.writeLock().unlock();
            }
        }

        @Override
        public void broadcastMessage(String message) {
            try {
                _hallChat.sendMessage("TournamentSystem", message, true);
            } catch (PrivateInformationException exp) {
                // Ignore, sent as admin
            } catch (ChatCommandErrorException e) {
                // Ignore, no command
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    /* Despite a thorough culling of most Lord of the Rings content in this code base for the Star Trek implementation,
        this is delightful and I never want to lose it. */
    private List<String> getIgnoredUserMessages() {
        List<String> result = new LinkedList<>();
        result.add("You don't have any friends. Nobody likes you.");
        result.add("Not listening. Not listening!");
        result.add("You're a liar and a thief.");
        result.add("Nope.");
        result.add("Murderer!");
        result.add("Go away. Go away!");
        result.add("Hahahaha!");
        result.add("I hate you, I hate you.");
        result.add("Where would you be without me? Gollum, Gollum. I saved us. It was me. We survived because of me!");
        result.add("Not anymore.");
        result.add("What did you say?");
        result.add("Master looks after us now. We don't need you.");
        result.add("What?");
        result.add("Leave now and never come back.");
        result.add("No!");
        result.add("Leave now and never come back!");
        result.add("Argh!");
        result.add("Leave NOW and NEVER COME BACK!");
        result.add("...");
        result.add("We... We told him to go away! And away he goes, preciouss! Gone, gone, gone! Smeagol is free!");
        return result;
    }
}