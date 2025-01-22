package com.gempukku.stccg.cards.blueprints.actionsource;

import com.gempukku.stccg.actions.turn.ActivateCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class ActivateCardActionSource extends DefaultActionSource {

    public ActivateCardAction createAction(PhysicalCard card) { return new ActivateCardAction(card.getGame(), card); }

    @Override
    protected ActivateCardAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
            ActivateCardAction action = createAction(card);
            appendActionToContext(action, actionContext);
            return action;
        }
        return null;
    }

}