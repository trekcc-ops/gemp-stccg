package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

import java.util.List;

public class ConditionTriggerChecker implements TriggerChecker {

    private final List<Requirement> _requirements;

    ConditionTriggerChecker(
            @JsonProperty(value = "requires", required = true)
            List<Requirement> requirements
    ) {
        _requirements = requirements;
    }
    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        return actionContext.acceptsAllRequirements(cardGame, _requirements);
    }


}