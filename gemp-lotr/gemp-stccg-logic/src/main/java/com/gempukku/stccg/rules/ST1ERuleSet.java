package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.modifiers.ModifiersLogic;

public class ST1ERuleSet extends RuleSet {
    private final ActionsEnvironment _actionsEnvironment;

    public ST1ERuleSet(ActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        super(actionsEnvironment, modifiersLogic);
        _actionsEnvironment = actionsEnvironment;
    }

    public void applySpecificRules() {
        new ST1EPlayCardInPhaseRule(_actionsEnvironment).applyRule();
        new ST1EExecuteOrdersRule(_actionsEnvironment).applyRule();
        new ST1EAffiliationAttackRestrictionsRule(_actionsEnvironment).applyRule();
//        new ActivatePhaseActionsRule(_actionsEnvironment).applyRule();
    }
}