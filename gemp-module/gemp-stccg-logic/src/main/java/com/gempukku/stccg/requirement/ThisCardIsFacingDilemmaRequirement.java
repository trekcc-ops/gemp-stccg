package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ThisCardIsFacingDilemmaRequirement implements Requirement {
    public boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        return getCondition(actionContext, actionContext.card(), cardGame).isFulfilled(cardGame);
    }

    @Override
    public Condition getCondition(ActionContext context, PhysicalCard thisCard, DefaultGame cardGame) {
        return new FacingDilemmaCondition(thisCard);
    }
}