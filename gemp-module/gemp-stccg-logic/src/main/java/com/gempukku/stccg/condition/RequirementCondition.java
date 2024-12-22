package com.gempukku.stccg.condition;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.game.DefaultGame;

public class RequirementCondition implements Condition {
    private final Requirement[] requirements;
    private final ActionContext actionContext;

    public RequirementCondition(Requirement[] requirements, ActionContext actionContext) {
        this.requirements = requirements;
        this.actionContext = actionContext;
    }

    @Override
    public boolean isFulfilled(DefaultGame cardGame) { return actionContext.acceptsAllRequirements(requirements); }
}