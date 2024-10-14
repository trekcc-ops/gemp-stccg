package com.gempukku.stccg.cards.blueprints.actionsource;

import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.RequiredType;
import com.gempukku.stccg.common.filterable.TriggerTiming;

public class OptionalTriggerActionSource extends TriggerActionSource {
    private final TriggerTiming _triggerTiming;
    private final RequiredType _requiredType = RequiredType.OPTIONAL;

    public OptionalTriggerActionSource(TriggerTiming triggerTiming) {
        _triggerTiming = triggerTiming;
    }

    public OptionalTriggerAction createAction(PhysicalCard card) {
        return new OptionalTriggerAction(card, this);
    }

    @Override
    protected OptionalTriggerAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
                OptionalTriggerAction action = createAction(card);
                appendActionToContext(action, actionContext);
                return action;
        }
        return null;
    }

    public TriggerTiming getTiming() { return _triggerTiming; }
    public RequiredType getRequiredType() { return _requiredType; }

}