package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class PhaseRequirement implements Requirement {

    private final Phase _phase;

    public PhaseRequirement(
            @JsonProperty(value = "phase", required = true)
            Phase phase) {
        _phase = phase;
    }

    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        Phase currentPhase = cardGame.getCurrentPhase();
        return currentPhase == _phase;
    }
}