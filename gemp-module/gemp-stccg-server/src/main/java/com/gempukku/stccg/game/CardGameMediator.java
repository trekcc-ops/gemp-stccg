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
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gameevent.GameStateListener;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.hall.GameSettings;
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
    private final boolean _showInGameHall;
    private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock.ReadLock _readLock = _lock.readLock();
    private final ReentrantReadWriteLock.WriteLock _writeLock = _lock.writeLock();
    private int _channelNextIndex;
    private volatile boolean _destroyed;
    private final DefaultGame _game;


    CardGameMediator(String gameId, GameParticipant[] participants, CardBlueprintLibrary blueprintLibrary,
                     GameSettings gameSettings) {
        _allowSpectators = (gameSettings.getLeague() != null) ||
                (!gameSettings.isCompetitive() && !gameSettings.isPrivateGame() && !gameSettings.isHiddenGame());
        _gameId = gameId;
        _timeSettings = gameSettings.getTimeSettings();
        _showInGameHall = gameSettings.isHiddenGame();
        if (participants.length < 1)
            throw new IllegalArgumentException("Game can't have less than one participant");

        for (GameParticipant participant : participants) {
            String participantId = participant.getPlayerId();
            CardDeck deck = participant.getDeck();
            _playerDecks.put(participantId, deck);
            _playerClocks.put(participantId, new PlayerClock(participantId, gameSettings.getTimeSettings()));
            _playersPlaying.add(participantId);
        }

        GameFormat gameFormat = gameSettings.getGameFormat();
        _game = switch (gameSettings.getGameType()) {
            case FIRST_EDITION -> new ST1EGame(gameFormat, _playerDecks, _playerClocks, blueprintLibrary);
            case SECOND_EDITION -> new ST2EGame(gameFormat, _playerDecks, _playerClocks, blueprintLibrary);
            case TRIBBLES -> new TribblesGame(gameFormat, _playerDecks, _playerClocks, blueprintLibrary);
        };
    }


    public final boolean isVisibleToUser(String username) {
        return !_showInGameHall || _playersPlaying.contains(username);
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

    public final boolean isAllowSpectators() {
        return _allowSpectators;
    }

    public final void setPlayerAutoPassSettings(User user, Set<Phase> phases) {
        String userId = user.getName();
        if (_playersPlaying.contains(userId)) {
            _game.setPlayerAutoPassSettings(userId, phases);
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
        _readLock.lock();
        try {
            PhysicalCard card = _game.getCardFromCardId(cardId);
            return CardInfoSerializer.serialize(_game, card);
        } catch (CardNotFoundException e) {
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND, e.getMessage());
        } finally {
            _readLock.unlock();
        }
    }

    public final void startGame() {
        _writeLock.lock();
        try {
            getGame().startGame();
            startClocksForUsersPendingDecision();
        } finally {
            _writeLock.unlock();
        }
    }

    public final void cleanup() {
        _writeLock.lock();
        try {
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
        } finally {
            _writeLock.unlock();
        }
    }

    public final void concede(User player) {
        String playerId = player.getName();
        _writeLock.lock();
        try {
            if (getGame().getWinnerPlayerId() == null && _playersPlaying.contains(playerId)) {
                addTimeSpentOnDecisionToUserClock(playerId);
                getGame().playerLost(playerId, "Concession");
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public final void cancel(User player) {

        String playerId = player.getName();
        _writeLock.lock();
        try {
            if (_playersPlaying.contains(playerId))
                _game.requestCancel(playerId);
        } finally {
            _writeLock.unlock();
        }
    }

    public final synchronized void playerAnswered(User player, int channelNumber, int decisionId, String answer)
            throws HttpProcessingException {
        _writeLock.lock();
        try {
            String playerName = player.getName();
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
            } catch (InvalidGameOperationException | RuntimeException runtimeException) {
                LOGGER.error(ERROR_MESSAGE, runtimeException);
            }
        } finally {
            _writeLock.unlock();
        }
    }


    public final GameCommunicationChannel getCommunicationChannel(User player, int channelNumber)
            throws HttpProcessingException {
        String playerName = player.getName();
        if (!player.hasType(User.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();

        _readLock.lock();
        try {
            GameCommunicationChannel communicationChannel = _communicationChannels.get(playerName);
            if (communicationChannel == null)
                throw new SubscriptionExpiredException();

            if (communicationChannel.getChannelNumber() == channelNumber)
                return communicationChannel;
            else
                throw new SubscriptionConflictException();

        } finally {
            _readLock.unlock();
        }
    }


    public final String serializeEventsToString(GameCommunicationChannel communicationChannel)
            throws IOException {
        _readLock.lock();
        try {
            ObjectMapper jsonMapper = new ObjectMapper();
            return jsonMapper.writeValueAsString(communicationChannel);
        } catch(IOException exp) {
            getGame().sendErrorMessage("Unable to serialize game events");
            throw new IOException(exp.getMessage());
        } finally {
            _readLock.unlock();
        }
    }

    public final String signupUserForGameAndGetGameState(User player)
            throws PrivateInformationException, JsonProcessingException {
        String playerName = player.getName();
        if (!player.hasType(User.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();
        GameCommunicationChannel channel;
        int channelNumber;

        _readLock.lock();
        try {
            channelNumber = _channelNextIndex;
            _channelNextIndex++;

            channel = new GameCommunicationChannel(getGame(), playerName, channelNumber);
            _communicationChannels.put(playerName, channel);
            _game.addGameStateListener(channel);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = getGame().getGameState().serializeForPlayer(player.getName());
            JsonNode gameState = mapper.readTree(jsonString);
            Map<String, Object> result = new HashMap<>();
            result.put("channelNumber", channelNumber);
            result.put("gameState", gameState);
            return mapper.writeValueAsString(result);
        } finally {
            _readLock.unlock();
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
        _readLock.lock();
        try {
            GameState gameState = getGame().getGameState();
            return gameState.serializeComplete();
        } finally {
            _readLock.unlock();
        }
    }

    public String serializeGameStateForPlayer(String playerId) throws JsonProcessingException {
        _readLock.lock();
        try {
            GameState gameState = getGame().getGameState();
            return gameState.serializeForPlayer(playerId);
        } finally {
            _readLock.unlock();
        }
    }

    public void initialize(GameRecorder gameRecorder, String tournamentName, List<GameResultListener> listeners) {
        GameFormat gameFormat = _game.getFormat();
        sendMessageToPlayers("You're starting a game of " + gameFormat.getName());
        StringBuilder players = new StringBuilder();
        Map<String, CardDeck> decks =  new HashMap<>();

        for (String playerName : _playersPlaying) {
            if (!players.isEmpty())
                players.append(", ");
            players.append(playerName);
            decks.put(playerName, _playerDecks.get(playerName));
        }

        sendMessageToPlayers("Players in the game are: " + players);

        final var gameRecordingInProgress = gameRecorder.recordGame(this, gameFormat, tournamentName, decks);

        listeners.add(new RecordingGameResultListener(_playersPlaying, gameRecordingInProgress));

        for (GameResultListener listener : listeners) {
            _game.addGameResultListener(listener);
        }
    }
}