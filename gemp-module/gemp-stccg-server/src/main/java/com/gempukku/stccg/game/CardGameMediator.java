package com.gempukku.stccg.game;

import com.gempukku.stccg.chat.PrivateInformationException;
import com.gempukku.stccg.SubscriptionConflictException;
import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.common.AwaitingDecision;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.hall.GameTimer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class CardGameMediator {
    protected static final Logger LOGGER = LogManager.getLogger(CardGameMediator.class);
    protected final Map<String, GameCommunicationChannel> _communicationChannels = Collections.synchronizedMap(new HashMap<>());
    protected final Map<String, CardDeck> _playerDecks = new HashMap<>();
    protected final Map<String, Integer> _playerClocks = new HashMap<>();
    protected final Map<String, Long> _decisionQuerySentTimes = new HashMap<>();
    protected final Set<String> _playersPlaying = new HashSet<>();

    protected final String _gameId;

    protected final GameTimer _timeSettings;
    protected final boolean _allowSpectators;
    protected final boolean _showInGameHall;

    protected final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock(true);
    protected final ReentrantReadWriteLock.ReadLock _readLock = _lock.readLock();
    protected final ReentrantReadWriteLock.WriteLock _writeLock = _lock.writeLock();
    protected int _channelNextIndex = 0;
    protected volatile boolean _destroyed;

    public CardGameMediator(String gameId, GameParticipant[] participants,
                            GameTimer gameTimer, boolean allowSpectators, boolean showInGameHall) {
        _gameId = gameId;
        _timeSettings = gameTimer;
        _allowSpectators = allowSpectators;
        this._showInGameHall = showInGameHall;
        if (participants.length < 1)
            throw new IllegalArgumentException("Game can't have less than one participant");

        for (GameParticipant participant : participants) {
            String participantId = participant.getPlayerId();
            _playerDecks.put(participantId, participant.getDeck());
            _playerClocks.put(participantId, 0);
            _playersPlaying.add(participantId);
        }
    }

    public boolean isVisibleToUser(String username) {
        return !_showInGameHall || _playersPlaying.contains(username);
    }

    public boolean isDestroyed() {
        return _destroyed;
    }

    public void destroy() {
        _destroyed = true;
    }

    public String getGameId() {
        return _gameId;
    }

    public abstract DefaultGame getGame();

    public boolean isAllowSpectators() {
        return _allowSpectators;
    }

    public void setPlayerAutoPassSettings(String playerId, Set<Phase> phases) {
        if (_playersPlaying.contains(playerId)) {
            getGame().setPlayerAutoPassSettings(playerId, phases);
        }
    }

    public void sendMessageToPlayers(String message) {
        getGame().sendMessage(message);
    }

    public void addGameStateListener(String playerId, GameCommunicationChannel listener) {
        getGame().addGameStateListener(playerId, listener);
    }

    public void addGameResultListener(GameResultListener listener) {
        getGame().addGameResultListener(listener);
    }

    public String getWinner() {
        return getGame().getWinnerPlayerId();
    }

    public List<String> getPlayersPlaying() {
        return new LinkedList<>(_playersPlaying);
    }

    public String getGameStatus() {
        if (getGame().isCancelled())
            return "Cancelled";
        if (getGame().isFinished())
            return "Finished";
        final Phase currentPhase = getGame().getGameState().getCurrentPhase();
        if (currentPhase == Phase.SEED_DOORWAY || currentPhase == Phase.SEED_FACILITY || currentPhase == Phase.SEED_MISSION || currentPhase == Phase.SEED_DILEMMA)
            return "Seeding";
        return "Playing";
    }

    public boolean isFinished() {
        return getGame().isFinished();
    }

    public String produceCardInfo(int cardId) {
        _readLock.lock();
        try {
            return getGame().produceCardInfo(cardId);
        } finally {
            _readLock.unlock();
        }
    }

    public void startGame() {
        _writeLock.lock();
        try {
            getGame().startGame();
            startClocksForUsersPendingDecision();
        } finally {
            _writeLock.unlock();
        }
    }

    public void cleanup() {
        _writeLock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            Map<String, GameCommunicationChannel> channelsCopy = new HashMap<>(_communicationChannels);
            for (Map.Entry<String, GameCommunicationChannel> playerChannels : channelsCopy.entrySet()) {
                String playerId = playerChannels.getKey();
                // Channel is stale (user no longer connected to game, to save memory, we remove the channel
                // User can always reconnect and establish a new channel
                GameCommunicationChannel channel = playerChannels.getValue();
                if (currentTime > channel.getLastAccessed() + _timeSettings.maxSecondsPerDecision() * 1000L) {
                    getGame().removeGameStateListener(channel);
                    _communicationChannels.remove(playerId);
                }
            }

            if (getGame() != null && getGame().getWinnerPlayerId() == null) {
                for (Map.Entry<String, Long> playerDecision : new HashMap<>(_decisionQuerySentTimes).entrySet()) {
                    String player = playerDecision.getKey();
                    long decisionSent = playerDecision.getValue();
                    if (currentTime > decisionSent + _timeSettings.maxSecondsPerDecision() * 1000L) {
                        addTimeSpentOnDecisionToUserClock(player);
                        getGame().playerLost(player, "Player decision timed-out");
                    }
                }

                for (Map.Entry<String, Integer> playerClock : _playerClocks.entrySet()) {
                    String player = playerClock.getKey();
                    if (_timeSettings.maxSecondsPerPlayer() - playerClock.getValue() - getCurrentUserPendingTime(player) < 0) {
                        addTimeSpentOnDecisionToUserClock(player);
                        getGame().playerLost(player, "Player run out of time");
                    }
                }
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public void concede(User player) {
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

    public void cancel(User player) {
//        getGame().getGameState().sendWarning(player.getName(), "You can't cancel this game");

        String playerId = player.getName();
        _writeLock.lock();
        try {
            if (_playersPlaying.contains(playerId))
                getGame().requestCancel(playerId);
        } finally {
            _writeLock.unlock();
        }
    }

    public synchronized void playerAnswered(User player, int channelNumber, int decisionId, String answer) throws SubscriptionConflictException, SubscriptionExpiredException {
        String playerName = player.getName();
        _writeLock.lock();
        try {
            GameCommunicationChannel communicationChannel = _communicationChannels.get(playerName);
            if (communicationChannel != null) {
                if (communicationChannel.getChannelNumber() == channelNumber) {
                    AwaitingDecision awaitingDecision = getGame().getAwaitingDecision(playerName);
                    if (awaitingDecision != null) {
                        if (awaitingDecision.getAwaitingDecisionId() == decisionId && !getGame().isFinished()) {
                            try {
                                getGame().getGameState().playerDecisionFinished(playerName);
                                awaitingDecision.decisionMade(answer);

                                // Decision successfully made, add the time to user clock
                                addTimeSpentOnDecisionToUserClock(playerName);

                                getGame().carryOutPendingActionsUntilDecisionNeeded();
                                startClocksForUsersPendingDecision();

                            } catch (DecisionResultInvalidException decisionResultInvalidException) {
                                // Participant provided wrong answer - send a warning message, and ask again for the same decision
                                getGame().getGameState().sendWarning(playerName, decisionResultInvalidException.getWarningMessage());
                                getGame().sendAwaitingDecision(playerName, awaitingDecision);
                            } catch (RuntimeException runtimeException) {
                                LOGGER.error("Error processing game decision", runtimeException);
                                getGame().cancelGame();
                            }
                        }
                    }
                } else {
                    throw new SubscriptionConflictException();
                }
            } else {
                throw new SubscriptionExpiredException();
            }
        } finally {
            _writeLock.unlock();
        }
    }

    public GameCommunicationChannel getCommunicationChannel(User player, int channelNumber) throws PrivateInformationException, SubscriptionConflictException, SubscriptionExpiredException {
        String playerName = player.getName();
        if (!player.hasType(User.Type.ADMIN) && !_allowSpectators && !_playersPlaying.contains(playerName))
            throw new PrivateInformationException();

        _readLock.lock();
        try {
            GameCommunicationChannel communicationChannel = _communicationChannels.get(playerName);
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
            _readLock.unlock();
        }
    }

    public void processVisitor(GameCommunicationChannel communicationChannel, int channelNumber, ParticipantCommunicationVisitor visitor) {
        _readLock.lock();
        try {
            visitor.visitChannelNumber(channelNumber);
            visitor.visitGameEvents(communicationChannel);
            visitor.visitClock(secondsLeft());
        } finally {
            _readLock.unlock();
        }
    }

    public void signupUserForGame(User player, ParticipantCommunicationVisitor visitor) throws PrivateInformationException {
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

            getGame().addGameStateListener(playerName, channel);
        } finally {
            _readLock.unlock();
        }
        processVisitor(channel, channelNumber, visitor);
    }

    private Map<String, Integer> secondsLeft() {
        Map<String, Integer> secondsLeft = new HashMap<>();
        for (String playerId : _playersPlaying) {
            secondsLeft.put(playerId, _timeSettings.maxSecondsPerPlayer() - _playerClocks.get(playerId) - getCurrentUserPendingTime(playerId));
        }
        return secondsLeft;
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
            _playerClocks.put(participantId, _playerClocks.get(participantId) + (int) diffSec);
        }
    }

    public int getCurrentUserPendingTime(String participantId) {
        if (!_decisionQuerySentTimes.containsKey(participantId))
            return 0;
        long queryTime = _decisionQuerySentTimes.get(participantId);
        long currentTime = System.currentTimeMillis();
        return (int) ((currentTime - queryTime) / 1000);
    }

    public GameTimer getTimeSettings() {
        return _timeSettings;
    }

    public Map<String, Integer> getPlayerClocks() { return Collections.unmodifiableMap(_playerClocks); }

}