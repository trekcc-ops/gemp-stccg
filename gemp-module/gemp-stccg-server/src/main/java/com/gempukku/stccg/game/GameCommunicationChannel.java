package com.gempukku.stccg.game;

import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.AwaitingDecision;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.gamestate.GameEvent;
import com.gempukku.stccg.gamestate.GameStateListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    private final DefaultGame _game;

    public GameCommunicationChannel(DefaultGame game, String playerId, int channelNumber) {
        _game = game;
        _playerId = playerId;
        _channelNumber = channelNumber;
    }

    public int getChannelNumber() {
        return _channelNumber;
    }

    public void initializeBoard() {
        appendEvent(new GameEvent(PARTICIPANTS, _game.getGameState(), _game.getGameState().getPlayer(_playerId)));
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
    public void setCurrentPhase(Phase phase) {
        appendEvent(new GameEvent(GAME_PHASE_CHANGE, phase));
    }

    @Override
    public void setPlayerDecked(Player player) {
        appendEvent(new GameEvent(PLAYER_DECKED, player));
    }

    @Override
    public void setPlayerScore(String participant, int points) {
        appendEvent(new GameEvent(PLAYER_SCORE, _game.getGameState().getPlayer(participant)));
    }

    @Override
    public void setTribbleSequence(String tribbleSequence) {
        appendEvent(new GameEvent(TRIBBLE_SEQUENCE_UPDATE, tribbleSequence));
    }

    @Override
    public void setCurrentPlayerId(String currentPlayerId) {
        appendEvent(new GameEvent(TURN_CHANGE, _game.getGameState().getPlayer(currentPlayerId)));
    }

    @Override
    public void sendMessage(String message) {
        appendEvent(new GameEvent(SEND_MESSAGE, message));
    }

    public void decisionRequired(String playerId, AwaitingDecision decision) {
        if (playerId.equals(_playerId))
            appendEvent(new GameEvent(DECISION, decision, _game.getGameState().getPlayer(playerId)));
    }

    @Override
    public void sendWarning(String playerId, String warning) {
        if (playerId.equals(_playerId))
            appendEvent(new GameEvent(SEND_WARNING, warning));
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

    public String produceCardInfo(int cardId) {
        PhysicalCard card = _game.getGameState().findCardById(cardId);
        if (card == null || card.getZone() == null)
            return null;
        else
            return card.getCardInfoHTML();
    }

    public void serializeConsumedEvents(Document doc, Element element) {
        for (GameEvent event : consumeGameEvents())
            element.appendChild(event.serialize(doc));
    }

}
