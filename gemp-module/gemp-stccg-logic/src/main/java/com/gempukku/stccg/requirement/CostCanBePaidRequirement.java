package com.gempukku.stccg.requirement;

import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class CostCanBePaidRequirement implements Requirement {

    private final SubActionBlueprint _costBlueprint;

    public CostCanBePaidRequirement(SubActionBlueprint cost) {
        _costBlueprint = cost;
    }

    @Override
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        return _costBlueprint.isPlayableInFull(cardGame, actionContext);
    }

}