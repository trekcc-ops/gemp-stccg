package com.gempukku.stccg.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;
import com.gempukku.stccg.gameevent.GameEvent;
import com.gempukku.stccg.gameevent.GameStateListener;
import com.gempukku.stccg.gameevent.SendMessageGameEvent;
import com.gempukku.stccg.player.PlayerClock;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GameCommunicationChannel implements GameStateListener, LongPollableResource {
    private List<GameEvent> _events = Collections.synchronizedList(new LinkedList<>());
    private final String _playerId;
    private ZonedDateTime _lastConsumed = ZonedDateTime.now(ZoneId.of("UTC"));
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
    public final void sendMessageEvent(String message) {
        appendEvent(new SendMessageGameEvent(GameEvent.Type.SEND_MESSAGE, message));
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

    @JsonProperty("playerClocks")
    public Collection<PlayerClock> getPlayerClocks() {
        return _game.getGameState().getPlayerClocks().values();
    }

    private void updateLastAccess() {
        _lastConsumed = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @JsonIgnore
    public final ZonedDateTime getLastAccessed() {
        return _lastConsumed;
    }

}