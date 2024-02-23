package com.gempukku.stccg.actions.sources;

import com.gempukku.stccg.actions.RequiredTriggerAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.common.filterable.RequiredType;

public class RequiredTriggerActionSource extends TriggerActionSource {

    private final TriggerTiming _triggerTiming;
    private final RequiredType _requiredType = RequiredType.REQUIRED;

    public RequiredTriggerActionSource() { _triggerTiming = null; }
    public RequiredTriggerActionSource(TriggerTiming triggerTiming) { _triggerTiming = triggerTiming; }
    @Override
    public RequiredTriggerAction createAction(PhysicalCard card) {
        return new RequiredTriggerAction(card);
    }

    @Override
    public RequiredTriggerAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
            RequiredTriggerAction action = createAction(card);
            appendActionToContext(action, actionContext);
            return action;
        }
        return null;
    }

    public TriggerTiming getTiming() { return _triggerTiming; }
    public RequiredType getRequiredType() { return _requiredType; }

}
