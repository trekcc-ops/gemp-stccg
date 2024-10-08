package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.condition.Condition;

public class RequirementCondition implements Condition {
    private final Requirement[] requirements;
    private final ActionContext actionContext;

    public RequirementCondition(Requirement[] requirements, ActionContext actionContext) {
        this.requirements = requirements;
        this.actionContext = actionContext;
    }

    @Override
    public boolean isFulfilled() { return actionContext.acceptsAllRequirements(requirements); }
}