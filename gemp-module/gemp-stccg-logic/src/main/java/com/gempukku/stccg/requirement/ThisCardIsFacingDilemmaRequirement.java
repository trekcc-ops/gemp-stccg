package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ThisCardIsFacingDilemmaRequirement implements Requirement {
    public boolean accepts(GameTextContext actionContext, DefaultGame cardGame) {
        return getCondition(actionContext, actionContext.card(), cardGame).isFulfilled(cardGame);
    }

    @Override
    public Condition getCondition(GameTextContext context, PhysicalCard thisCard, DefaultGame cardGame) {
        return new FacingDilemmaCondition(thisCard);
    }
}