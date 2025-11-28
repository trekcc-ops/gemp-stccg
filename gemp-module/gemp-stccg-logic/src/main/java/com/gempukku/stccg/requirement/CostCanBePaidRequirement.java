package com.gempukku.stccg.requirement;

import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.cards.ActionContext;

public class CostCanBePaidRequirement implements Requirement {

    private final SubActionBlueprint _costBlueprint;

    public CostCanBePaidRequirement(SubActionBlueprint cost) {
        _costBlueprint = cost;
    }

    @Override
    public boolean accepts(ActionContext actionContext) {
        return _costBlueprint.isPlayableInFull(actionContext);
    }
}