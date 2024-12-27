package com.gempukku.stccg.actions.revealcards;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class RevealCardFromHandResult extends ActionResult {

    public RevealCardFromHandResult(PhysicalCard source) {
        super(Type.FOR_EACH_REVEALED_FROM_HAND, source);
    }

}