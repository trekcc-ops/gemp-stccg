package com.gempukku.stccg.actions.sources;

import com.gempukku.stccg.actions.OptionalTriggerAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.ActionLimitType;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.common.filterable.RequiredType;

public class OptionalTriggerActionSource extends TriggerActionSource {
    private final TriggerTiming _triggerTiming;
    private final RequiredType _requiredType = RequiredType.OPTIONAL;
    private final ActionLimitType _limitType;

    public OptionalTriggerActionSource() {
        _triggerTiming = null;
        _limitType = ActionLimitType.UNLIMITED;
    }

    public OptionalTriggerActionSource(TriggerTiming triggerTiming, ActionLimitType limitType) {
        super();
        _limitType = limitType;
        _triggerTiming = triggerTiming;
    }

    @Override
    public OptionalTriggerAction createAction(PhysicalCard card) {
        return new OptionalTriggerAction(card, this);
    }

    @Override
    public OptionalTriggerAction createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        if (isValid(actionContext)) {
            if (_limitType == ActionLimitType.ONCE_EACH_TURN) {
                int usedCount = actionContext.getGame().getModifiersQuerying()
                        .getUntilEndOfTurnLimitCounter(this).getUsedLimit();
                if (usedCount < 1 && card.isControlledBy(actionContext.getGame().getCurrentPlayer())) {
                    OptionalTriggerAction action = createAction(card);
                    appendActionToContext(action, actionContext);
                    return action;
                }
            } else {
                OptionalTriggerAction action = createAction(card);
                appendActionToContext(action, actionContext);
                return action;
            }
        }
        return null;
    }

    public TriggerTiming getTiming() { return _triggerTiming; }
    public RequiredType getRequiredType() { return _requiredType; }

}
