package com.gempukku.stccg.game;

import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;
import com.gempukku.stccg.common.AwaitingDecision;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.GameEvent;
import com.gempukku.stccg.gamestate.GameStateListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.gempukku.stccg.gamestate.GameEvent.Type.*;

public class GameCommunicationChannel implements GameStateListener, LongPollableResource {
    private List<GameEvent> _events = Collections.synchronizedList(new LinkedList<>());
    private final String _playerId;
    private long _lastConsumed = System.currentTimeMillis();
    private final int _channelNumber;
    private volatile WaitingRequest _waitingRequest;

    public GameCommunicationChannel(String self, int channelNumber, GameFormat format) {
        _playerId = self;
        _channelNumber = channelNumber;
    }

    public int getChannelNumber() {
        return _channelNumber;
    }

    @Override
    public void initializeBoard(List<String> participants, boolean discardIsPublic) {
        List<String> participantIds = new LinkedList<>(participants);
        appendEvent(new GameEvent(PARTICIPANTS)
                .participantId(_playerId)
                .allParticipantIds(participantIds)
                .discardPublic(discardIsPublic)
        );
    }

    public String getPlayerId() { return _playerId; }

    @Override
    public synchronized void deregisterRequest(WaitingRequest waitingRequest) {
        _waitingRequest = null;
    }

    @Override
    public synchronized boolean registerRequest(WaitingRequest waitingRequest) {
        if (!_events.isEmpty())
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    private synchronized void appendEvent(GameEvent event) {
        _events.add(event);
        if (_waitingRequest != null) {
            _waitingRequest.processRequest();
            _waitingRequest = null;
        }
    }

    public void sendEvent(GameEvent gameEvent) {
        appendEvent(gameEvent);
    }
    public void sendEvent(GameEvent.Type eventType) {
        appendEvent(new GameEvent(eventType));
    }

    @Override
    public void setCurrentPhase(String phase) {
        appendEvent(new GameEvent(GAME_PHASE_CHANGE).phase(phase));
    }

    @Override
    public void setPlayerDecked(Player player, boolean bool) {
        appendEvent(new GameEvent(PLAYER_DECKED, player).bool(bool));
    }

    @Override
    public void setPlayerScore(String participant, int points) {
        appendEvent(new GameEvent(PLAYER_SCORE).participantId(participant).score(points));
    }

    @Override
    public void setTribbleSequence(String tribbleSequence) {
        appendEvent(new GameEvent(TRIBBLE_SEQUENCE_UPDATE).message(tribbleSequence));
    }

    @Override
    public void setCurrentPlayerId(String currentPlayerId) {
        appendEvent(new GameEvent(TURN_CHANGE).participantId(currentPlayerId));
    }

    @Override
    public void sendMessage(String message) {
        appendEvent(new GameEvent(SEND_MESSAGE).message(message));
    }

    public void decisionRequired(String playerId, AwaitingDecision decision) {
        if (playerId.equals(_playerId))
            appendEvent(new GameEvent(DECISION).awaitingDecision(decision).participantId(playerId));
    }

    @Override
    public void sendWarning(String playerId, String warning) {
        if (playerId.equals(_playerId))
            appendEvent(new GameEvent(SEND_WARNING).message(warning));
    }

    public List<GameEvent> consumeGameEvents() {
        updateLastAccess();
        List<GameEvent> result = _events;
        _events = Collections.synchronizedList(new LinkedList<>());
        return result;
    }

    private void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    public long getLastAccessed() {
        return _lastConsumed;
    }

    @Override
    public void endGame() {
        appendEvent(new GameEvent(GAME_ENDED));
    }

}
