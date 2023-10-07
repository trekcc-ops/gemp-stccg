package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.DefaultActionContext;
import com.gempukku.lotro.condition.Condition;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.requirement.Requirement;
import com.gempukku.lotro.requirement.RequirementUtils;

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
