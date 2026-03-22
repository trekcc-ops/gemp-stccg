package com.gempukku.stccg.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.GameTimer;

@JsonIgnoreProperties(value = { "timeRemaining" }, allowGetters = true)
public class PlayerClock {
    private int _timeElapsed;
    private final String _playerId;
    private final int _maxTimeAllowed;

    public PlayerClock(
            @JsonProperty("playerId")
            String playerId,
            @JsonProperty("maxTimeAllowed")
            int maxTimeAllowed,
            @JsonProperty("timeElapsed")
            int timeElapsed
    ) {
        _timeElapsed = timeElapsed;
        _playerId = playerId;
        _maxTimeAllowed = maxTimeAllowed;
    }

    public PlayerClock(String playerId, GameTimer settings) {
        _timeElapsed = 0;
        _playerId = playerId;
        _maxTimeAllowed = settings.maxSecondsPerPlayer();
    }

    @JsonProperty("timeElapsed")
    public int getTimeElapsed() {
        return _timeElapsed;
    }

    @JsonProperty("timeRemaining")
    private int getTimeRemainingSerialized() {
        return _maxTimeAllowed - _timeElapsed;
    }

    @JsonProperty("maxTimeAllowed")
    private int getMaxTimeAllowed() { return _maxTimeAllowed; }

    @JsonProperty("playerId")
    public String getPlayerId() {
        return _playerId;
    }

    public void addElapsedTime(int diffSec) {
        _timeElapsed = _timeElapsed + diffSec;
    }

}