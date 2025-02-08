package com.gempukku.stccg.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.GameTimer;

public class PlayerClock {
    private int _timeElapsed = 0;
    private final String _playerId;
    private final GameTimer _settings;

    public PlayerClock(String playerId, GameTimer settings) {
        _playerId = playerId;
        _settings = settings;
    }

    @JsonIgnore
    public int getTimeElapsed() {
        return _timeElapsed;
    }

    @JsonProperty("timeRemaining")
    private int getTimeRemainingSerialized() {
        return _settings.maxSecondsPerPlayer() - _timeElapsed;
    }

    @JsonProperty("playerId")
    public String getPlayerId() {
        return _playerId;
    }

    public void addElapsedTime(int diffSec) {
        _timeElapsed = _timeElapsed + diffSec;
    }

}