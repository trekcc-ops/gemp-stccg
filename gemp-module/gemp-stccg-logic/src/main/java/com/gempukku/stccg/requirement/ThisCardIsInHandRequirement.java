package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ThisCardIsInHandRequirement implements Requirement {

    @Override
    public boolean isTrue(PhysicalCard thisCard, DefaultGame cardGame) {
        return thisCard.isInHand(cardGame);
    }

}