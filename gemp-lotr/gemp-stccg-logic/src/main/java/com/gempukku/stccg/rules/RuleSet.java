package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.ActionsEnvironment;

public class RuleSet {
    private final ActionsEnvironment _actionsEnvironment;

    public RuleSet(ActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyGenericRules() {
        // CL - I see these less as "rules" and more as basic turn loop mechanisms
        new RequiredTriggersRule(_actionsEnvironment).applyRule();
        new OptionalTriggersRule(_actionsEnvironment).applyRule();
        new DiscardedCardRule(_actionsEnvironment).applyRule();
        new ActivateResponseAbilitiesRule(_actionsEnvironment).applyRule(); // Less sure about this one
    }

    public void applySpecificRules() {
            // Some of these may be more general
        new ActivatePhaseActionsRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromHandRule(_actionsEnvironment).applyRule();
        new OptionalTriggersFromHandRule(_actionsEnvironment).applyRule();
    }

    public void applyRuleSet() {
        applyGenericRules();
        applySpecificRules();
    }
}
