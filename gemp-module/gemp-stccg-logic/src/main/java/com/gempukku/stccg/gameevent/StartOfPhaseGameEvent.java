package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.filterable.Phase;

public class StartOfPhaseGameEvent extends GameEvent {

    private final Phase _phase;

    public StartOfPhaseGameEvent(Phase phase) {
        super(Type.GAME_PHASE_CHANGE);
        _phase = phase;
        _eventAttributes.put(Attribute.phase, phase.name());
    }

    @JsonProperty("phase")
    private final String getPhase() {
        return _phase.toString();
    }

}