package com.gempukku.stccg.cards.blueprints.actionsource;

import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class ActivateCardActionSource extends DefaultActionSource {

    @Override
    public ActivateCardAction createAction(PhysicalCard card) { return new ActivateCardAction(card); }

    @Override
    public ActivateCardAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
            ActivateCardAction action = createAction(card);
            appendActionToContext(action, actionContext);
            return action;
        }
        return null;
    }

}
