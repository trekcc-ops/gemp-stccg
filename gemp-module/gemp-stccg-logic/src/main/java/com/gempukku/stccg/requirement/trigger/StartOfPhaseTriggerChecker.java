package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class StartOfPhaseTriggerChecker implements TriggerChecker {

    private final Phase _phase;

    StartOfPhaseTriggerChecker(@JsonProperty(value = "phase", required = true)
                 Phase phase
    ) {
        _phase = phase;
    }

    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        return cardGame.isCurrentActionResultType(ActionResult.Type.START_OF_PHASE) &&
                cardGame.getGameState().getCurrentPhase() == _phase;
    }

}