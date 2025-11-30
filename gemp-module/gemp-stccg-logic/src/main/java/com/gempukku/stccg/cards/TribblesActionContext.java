package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class TribblesActionContext extends DefaultActionContext {

    public TribblesActionContext(ActionContext delegate, String performingPlayer,
                                 PhysicalCard source, ActionResult actionResult) {
        super(delegate, performingPlayer, source, actionResult);
    }

}