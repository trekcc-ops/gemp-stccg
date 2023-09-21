package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.requirement.Requirement;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.condition.Condition;

public class RequirementCondition implements Condition {
    private final Requirement[] requirements;
    private final DefaultActionContext actionContext;

    public RequirementCondition(Requirement[] requirements, DefaultActionContext actionContext) {
        this.requirements = requirements;
        this.actionContext = actionContext;
    }

    @Override
    public boolean isFullfilled(DefaultGame game) {
        for (Requirement requirement : requirements) {
            if (!requirement.accepts(actionContext))
                return false;
        }

        return true;
    }
}
