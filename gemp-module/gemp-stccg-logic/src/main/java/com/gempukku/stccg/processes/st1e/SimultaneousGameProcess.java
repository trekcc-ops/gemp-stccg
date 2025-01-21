package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.game.ST1EGame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class SimultaneousGameProcess extends ST1EGameProcess {

    @JsonProperty("playersParticipating")
    protected final List<String> _playersParticipating;

    SimultaneousGameProcess(Collection<String> playersParticipating) {
        super();
        _playersParticipating = new LinkedList<>(playersParticipating);
    }

}