package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.condition.Condition;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;

public class RequirementCondition implements Condition {
    private final Requirement[] requirements;
    private final DefaultActionContext actionContext;

    public RequirementCondition(Requirement[] requirements, DefaultActionContext actionContext) {
        this.requirements = requirements;
        this.actionContext = actionContext;
    }

    @Override
    public boolean isFulfilled(DefaultGame game) {
        return RequirementUtils.acceptsAllRequirements(requirements, actionContext);
    }
}
