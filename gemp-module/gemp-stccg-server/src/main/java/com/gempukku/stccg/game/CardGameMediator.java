package com.gempukku.stccg.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.SubscriptionConflictException;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.CloseableReadLock;
import com.gempukku.stccg.common.CloseableWriteLock;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.GameType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gameevent.GameStateListener;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.PlayerClock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CardGameMediator {
    private static final long MILLIS_TO_SECONDS = 1000L;
    private static final Logger LOGGER = LogManager.getLogger(CardGameMediator.class);
    private static final String ERROR_MESSAGE = "Error processing game decision";
    private final Map<String, GameCommunicationChannel> _communicationChannels =
            Collections.synchronizedMap(new HashMap<>());
    final Map<String, CardDeck> _playerDecks = new HashMap<>();
    protected final Map<String, PlayerClock> _playerClocks = new HashMap<>();
    private final Map<String, Long> _decisionQuerySentTimes = new HashMap<>();
    private final Set<String> _playersPlaying = new HashSet<>();
    private final String _gameId;
    private final GameTimer _timeSettings;
    private final boolean _allowSpectators;
    protected final Map<String, Set<Phase>> _autoPassConfiguration = new HashMap<>();

    private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock(true);
    private final CloseableReadLock _readLock = new CloseableReadLock(_lock);
    private final CloseableWriteLock _writeLock = new CloseableWriteLock(_lock);
    private int _channelNextIndex;
    private volatile boolean _destroyed;
    private final DefaultGame _game;
    protected final Set<GameResultListener> _gameResultListeners = new HashSet<>();
    private final Set<String> _requestedCancel = new HashSet<>();


    CardGameMediator(String gameId, GameParticipant[] participants, CardBlueprintLibrary blueprintLibrary,
                            boolean allowSpectators, GameTimer timeSettings, GameFormat gameFormat, GameType gameType) {
        _allowSpectators = allowSpectators;
        _gameId = gameId;
        _timeSettings = timeSettings;
        if (participants.length < 1)
            throw new IllegalArgumentException("Game can't have less than one participant");

        for (GameParticipant participant : participants) {
            String participantId = participant.getPlayerId();
            CardDeck deck = participant.getDeck();
            _playerDecks.put(participantId, deck);
            _playerClocks.put(participantId, new PlayerClock(participantId, timeSettings));
            _playersPlaying.add(participantId);
        }

        GameResultListener listener = new GameMediatorListener(this);

        _game = switch (gameType) {
            case FIRST_EDITION -> new ST1EGame(gameFormat, _playerDecks, _playerClocks, blueprintLibrary, listener);
            case SECOND_EDITION -> new ST2EGame(gameFormat, _playerDecks, _playerClocks, blueprintLibrary, listener);
            case TRIBBLES -> new TribblesGame(gameFormat, _playerDecks, _playerClocks, blueprintLibrary, listener);
        };

        // Game may be immediately cancelled if an error comes up while initializing game
        if (_game.isCancelled()) {
            cancelGameDueToError();
        }
    }


    public final boolean isDestroyed() {
        return _destroyed;
    }

    public final void destroy() {
        _destroyed = true;
    }

    public final String getGameId() {
        return _gameId;
    }

    public DefaultGame getGame() {
        return _game;
    }

    public final void setPlayerAutoPassSettings(String userName, Set<Phase> phases) {
        if (_playersPlaying.contains(userName)) {
            _autoPassConfiguration.put(userName, phases);
        }
    }


    final void sendMessageToPlayers(String message) {
        _game.sendMessage(message);
    }

    public final String getWinner() {
        return _game.getWinnerPlayerId();
    }

    public final List<String> getPlayersPlaying() {
        return new LinkedList<>(_playersPlaying);
    }

    public final String produceCardInfo(int cardId) throws JsonProcessingException, HttpProcessingException {
        try (CloseableReadLock ignored = _readLock.open()) {
            PhysicalCard card = _game.getCardFromCardId(cardId);
            return CardInfoSerializer.serialize(_game, card);
        } catch (CardNotFoundException e) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND, e.getMessage());
        }
    }

    public final void startGame() {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            _game.startGame();
            startClocksForUsersPendingDecision();
        }
    }

    public final void cleanup() {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            long currentTime = System.currentTimeMillis();
            Map<String, GameCommunicationChannel> channelsCopy = new HashMap<>(_communicationChannels);
            for (Map.Entry<String, GameCommunicationChannel> playerChannels : channelsCopy.entrySet()) {
                String playerId = playerChannels.getKey();
                // Channel is stale (user no longer connected to game, to save memory, we remove the channel
                // User can always reconnect and establish a new channel
                GameStateListener channel = playerChannels.getValue();
                if (currentTime >
                        channel.getLastAccessed() + _timeSettings.maxSecondsPerDecision() * MILLIS_TO_SECONDS) {
                    _game.removeGameStateListener(channel);
                    _communicationChannels.remove(playerId);
                }
            }

            if (_game.getWinnerPlayerId() == null) {
                Map<String, Long> decisionTimes = new HashMap<>(_decisionQuerySentTimes);
                for (Map.Entry<String, Long> playerDecision : decisionTimes.entrySet()) {
                    String player = playerDecision.getKey();
                    long decisionSent = playerDecision.getValue();
                    if (currentTime > decisionSent + _timeSettings.maxSecondsPerDecision() * MILLIS_TO_SECONDS) {
                        addTimeSpentOnDecisionToUserClock(player);
                        _game.playerLost(player, "Player decision timed-out");
                    }
                }

                for (PlayerClock playerClock : _playerClocks.values()) {
                    String player = playerClock.getPlayerId();
                    if (_timeSettings.maxSecondsPerPlayer() -
                            playerClock.getTimeElapsed() - getCurrentUserPendingTime(player) < 0) {
                        addTimeSpentOnDecisionToUserClock(player);
                        _game.playerLost(player, "Player run out of time");
                    }
                }
            }
        }
    }

    public final void concede(String userName) {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            if (getGame().getWinnerPlayerId() == null && _playersPlaying.contains(userName)) {
                addTimeSpentOnDecisionToUserClock(userName);
                getGame().playerLost(userName, "Concession");
            }
        }
    }

    public final void cancel(String userName) {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            if (_playersPlaying.contains(userName)) {
                _requestedCancel.add(userName);
                if (_requestedCancel.size() == _playersPlaying.size() && !isFinished()) {
                    _game.setCancelled(true);
                    _game.sendMessage("Game was cancelled, as requested by all parties.");
                    for (GameResultListener gameResultListener : _gameResultListeners)
                        gameResultListener.gameCancelled();
                    _game.setFinished(true);
                }
            }
        }
    }

    public void cancelGameDueToError() {
        if (!isFinished()) {
            _game.setCancelled(true);
            _game.sendMessage(
                        "Game was cancelled due to an error, the error was logged and will be fixed soon.");
            _game.sendMessage(
                        "Please post the replay game link and description of what happened on the tech support forum.");
        }
        for (GameResultListener gameResultListener : _gameResultListeners)
            gameResultListener.gameCancelled();
        _game.setFinished(true);
    }


    public final synchronized void playerAnswered(String playerName, int channelNumber, int decisionId, String answer)
            throws HttpProcessingException {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            GameStateListener communicationChannel = _communicationChannels.get(playerName);
            if (communicationChannel == null)
                throw new SubscriptionExpiredException();
            if (communicationChannel.getChannelNumber() != channelNumber)
                throw new SubscriptionConflictException();
            try {
                boolean decisionAccepted = _game.processUserDecision(playerName, decisionId, answer);
                if (decisionAccepted) {
                    addTimeSpentOnDecisionToUserClock(playerName);
                    _game.carryOutPendingActionsUntilDecisionNeeded();
                    startClocksForUsersPendingDecision();
                }
            } catch (InvalidGameOperationException | RuntimeException exp) {
                LOGGER.error(ERROR_MESSAGE, exp);
                cancelGameDueToError();
            }
        }
    }

    private boolean userCannotAccessGameState(User player) {
        return !player.hasType(User.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(player.getName());
    }


    public final GameCommunicationChannel getCommunicationChannel(User player, int channelNumber)
            throws HttpProcessingException {
        String playerName = player.getName();
        if (userCannotAccessGameState(player))
            throw new PrivateInformationException();

        try (CloseableReadLock ignored = _readLock.open()) {
            GameCommunicationChannel communicationChannel = _communicationChannels.get(playerName);
            if (communicationChannel == null)
                throw new SubscriptionExpiredException();

            if (communicationChannel.getChannelNumber() == channelNumber)
                return communicationChannel;
            else
                throw new SubscriptionConflictException();
        }
    }


    public final String serializeEventsToString(GameCommunicationChannel communicationChannel)
            throws IOException {
        try (CloseableReadLock ignored = _readLock.open()) {
            ObjectMapper jsonMapper = new ObjectMapper();
            return jsonMapper.writeValueAsString(communicationChannel);
        } catch(IOException exp) {
            getGame().sendErrorMessage("Unable to serialize game events");
            throw new IOException(exp.getMessage());
        }
    }

    public final String signupUserForGameAndGetGameState(String userName) throws JsonProcessingException {
        GameCommunicationChannel channel;
        int channelNumber;
        try (CloseableReadLock ignored = _readLock.open()) {
            channelNumber = _channelNextIndex;
            _channelNextIndex++;
            channel = new GameCommunicationChannel(getGame(), userName, channelNumber);
            _communicationChannels.put(userName, channel);
            _game.addGameStateListener(channel);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = _game.getGameState().serializeForPlayer(userName);
            JsonNode gameState = mapper.readTree(jsonString);
            Map<String, Object> result = new HashMap<>();
            result.put("channelNumber", channelNumber);
            result.put("gameState", gameState);
            return mapper.writeValueAsString(result);
        }
    }


    private void startClocksForUsersPendingDecision() {
        long currentTime = System.currentTimeMillis();
        Set<String> users = getGame().getUsersPendingDecision();
        for (String user : users)
            _decisionQuerySentTimes.put(user, currentTime);
    }

    private void addTimeSpentOnDecisionToUserClock(String participantId) {
        Long queryTime = _decisionQuerySentTimes.remove(participantId);
        if (queryTime != null) {
            long currentTime = System.currentTimeMillis();
            long diffSec = (currentTime - queryTime) / 1000;
            //noinspection NumericCastThatLosesPrecision
            PlayerClock playerClock = _playerClocks.get(participantId);
            playerClock.addElapsedTime((int) diffSec);
        }
    }

    private int getCurrentUserPendingTime(String participantId) {
        int result = 0;
        if (_decisionQuerySentTimes.containsKey(participantId)) {
            long queryTime = _decisionQuerySentTimes.get(participantId);
            long currentTime = System.currentTimeMillis();
            //noinspection NumericCastThatLosesPrecision
            result = (int) ((currentTime - queryTime) / 1000);
        }
        return result;
    }

    final GameTimer getTimeSettings() {
        return _timeSettings;
    }

    final Map<String, Integer> getPlayerClocks() {
        Map<String, Integer> result = new HashMap<>();
        for (String playerId : _playerClocks.keySet()) {
            result.put(playerId, _playerClocks.get(playerId).getTimeElapsed());
        }
        return result;
    }

    public String serializeCompleteGameState() throws JsonProcessingException {
        try (CloseableReadLock ignored = _readLock.open()) {
            return _game.serializeCompleteGameState();
        }
    }

    public String serializeGameStateForPlayer(String playerId) throws JsonProcessingException {
        try (CloseableReadLock ignored = _readLock.open()) {
            GameState gameState = getGame().getGameState();
            return gameState.serializeForPlayer(playerId);
        }
    }

    public void initialize(GameHistoryService gameHistory, String tournamentName, List<GameResultListener> listeners) {
        GameFormat gameFormat = _game.getFormat();
        sendMessageToPlayers("You're starting a game of " + gameFormat);
        StringBuilder players = new StringBuilder();
        Map<String, CardDeck> decks =  new HashMap<>();

        for (String playerName : _playersPlaying) {
            if (!players.isEmpty())
                players.append(", ");
            players.append(playerName);
            decks.put(playerName, _playerDecks.get(playerName));
        }

        sendMessageToPlayers("Players in the game are: " + players);

        final var gameRecordingInProgress = gameHistory.recordGame(this, gameFormat, tournamentName, decks);

        listeners.add(new RecordingGameResultListener(_playersPlaying, gameRecordingInProgress));
        _gameResultListeners.addAll(listeners);
    }

    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserReasons) {
        for (GameResultListener listener : _gameResultListeners) {
            listener.gameFinished(winnerPlayerId, winReason, loserReasons);
        }
    }

    public boolean isFinished() {
        return _game.isFinished();
    }

    public String getStatus() {
        return _game.getStatus();
    }

    public boolean hasPlayer(String playerName) {
        return _playersPlaying.contains(playerName);
    }

    public boolean allowsSpectators() {
        return _allowSpectators;
    }

}