package com.gempukku.stccg.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.gameevent.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

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

    @JsonProperty("channelNumber")
    public final int getChannelNumber() {
        return _channelNumber;
    }

    public final void initializeBoard() throws PlayerNotFoundException {
        appendEvent(new InitializeBoardForPlayerGameEvent(_game, _game.getGameState().getPlayer(_playerId)));
    }

    @JsonIgnore
    public final String getPlayerId() { return _playerId; }

    @Override
    public final synchronized void deregisterRequest() {
        _waitingRequest = null;
    }

    @Override
    @JsonIgnore
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

    @Override
    public final void setPlayerDecked(DefaultGame cardGame, Player player) {
        appendEvent(new GameEvent(GameEvent.Type.PLAYER_DECKED, player));
    }

    @Override
    public final void setPlayerScore(Player player) {
        appendEvent(new GameEvent(GameEvent.Type.PLAYER_SCORE, player));
    }

    @Override
    public final void setTribbleSequence(String tribbleSequence) {
        appendEvent(new UpdateTribbleSequenceGameEvent(tribbleSequence));
    }

    @Override
    public final void setCurrentPlayerId(String playerId) {
        try {
            appendEvent(new GameEvent(GameEvent.Type.TURN_CHANGE, _game.getPlayer(playerId)));
        } catch(PlayerNotFoundException exp) {
            _game.sendErrorMessage(exp);
            _game.cancelGame();
        }
    }

    @Override
    public final void sendMessage(String message) {
        appendEvent(new SendMessageGameEvent(GameEvent.Type.SEND_MESSAGE, message));
    }

    public final void decisionRequired(String playerId, AwaitingDecision awaitingDecision)
            throws PlayerNotFoundException {
        appendEvent(new SendDecisionGameEvent(_game, awaitingDecision, _game.getGameState().getPlayer(playerId)));
    }

    @Override
    public final void sendWarning(String playerId, String warning) {
        if (playerId.equals(_playerId))
            appendEvent(new SendMessageGameEvent(GameEvent.Type.SEND_WARNING, warning));
    }

    @JsonProperty("gameEvents")
    public final List<GameEvent> consumeGameEvents() {
        updateLastAccess();
        List<GameEvent> result = _events;
        _events = Collections.synchronizedList(new LinkedList<>());
        return result;
    }

    @JsonProperty("clocks")
    public Collection<PlayerClock> getPlayerClocks() {
        return _game.getPlayerClocks().values();
    }

    private void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    @JsonIgnore
    public final long getLastAccessed() {
        return _lastConsumed;
    }

}