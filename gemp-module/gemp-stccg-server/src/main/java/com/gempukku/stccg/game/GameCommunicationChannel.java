package com.gempukku.stccg.game;

import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;
import com.gempukku.stccg.common.AwaitingDecision;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.gamestate.GameEvent;
import com.gempukku.stccg.gamestate.GameStateListener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GameCommunicationChannel implements GameStateListener, LongPollableResource {
    private List<GameEvent> _events = Collections.synchronizedList(new LinkedList<>());
    private final String _playerId;
    private long _lastConsumed = System.currentTimeMillis();
    private final int _channelNumber;
    private volatile WaitingRequest _waitingRequest;
    private final DefaultGame _game;

    public GameCommunicationChannel(DefaultGame game, String playerId, int channelNumber) {
        _game = game;
        _playerId = playerId;
        _channelNumber = channelNumber;
    }

    public final int getChannelNumber() {
        return _channelNumber;
    }

    public final void initializeBoard() {
        appendEvent(new GameEvent(GameEvent.Type.PARTICIPANTS, _game.getGameState(),
                _game.getGameState().getPlayer(_playerId)));
    }

    public final String getPlayerId() { return _playerId; }

    @Override
    public final synchronized void deregisterRequest() {
        _waitingRequest = null;
    }

    @Override
    public final synchronized boolean registerRequest(WaitingRequest waitingRequest) {
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

    public final void sendEvent(GameEvent gameEvent) {
        appendEvent(gameEvent);
    }
    public final void sendEvent(GameEvent.Type eventType) {
        appendEvent(new GameEvent(eventType));
    }

    @Override
    public final void setCurrentPhase(Phase phase) {
        appendEvent(new GameEvent(GameEvent.Type.GAME_PHASE_CHANGE, phase));
    }

    @Override
    public final void setPlayerDecked(Player player) {
        appendEvent(new GameEvent(GameEvent.Type.PLAYER_DECKED, player));
    }

    @Override
    public final void setPlayerScore(String playerId) {
        appendEvent(new GameEvent(GameEvent.Type.PLAYER_SCORE, _game.getGameState().getPlayer(playerId)));
    }

    @Override
    public final void setTribbleSequence(String tribbleSequence) {
        appendEvent(new GameEvent(GameEvent.Type.TRIBBLE_SEQUENCE_UPDATE, tribbleSequence));
    }

    @Override
    public final void setCurrentPlayerId(String playerId) {
        appendEvent(new GameEvent(GameEvent.Type.TURN_CHANGE, _game.getGameState().getPlayer(playerId)));
    }

    @Override
    public final void sendMessage(String message) {
        appendEvent(new GameEvent(GameEvent.Type.SEND_MESSAGE, message));
    }

    public final void decisionRequired(String playerId, AwaitingDecision awaitingDecision) {
        if (playerId.equals(_playerId))
            appendEvent(new GameEvent(GameEvent.Type.DECISION, awaitingDecision,
                    _game.getGameState().getPlayer(playerId)));
    }

    @Override
    public final void sendWarning(String playerId, String warning) {
        if (playerId.equals(_playerId))
            appendEvent(new GameEvent(GameEvent.Type.SEND_WARNING, warning));
    }

    public final List<GameEvent> consumeGameEvents() {
        updateLastAccess();
        List<GameEvent> result = _events;
        _events = Collections.synchronizedList(new LinkedList<>());
        return result;
    }

    private void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    public final long getLastAccessed() {
        return _lastConsumed;
    }

    public final void serializeConsumedEvents(Document doc, Node element) {
        for (GameEvent event : consumeGameEvents())
            element.appendChild(event.serialize(doc));
    }

}