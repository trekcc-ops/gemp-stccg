package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.rules.RuleSet;

public class ST1ERuleSet extends RuleSet {
    private final ActionsEnvironment _actionsEnvironment;

    public ST1ERuleSet(ActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        super(actionsEnvironment);
        _actionsEnvironment = actionsEnvironment;
    }

    public void applySpecificRules() {
        new ST1EPlayCardInPhaseRule(_actionsEnvironment).applyRule();
        new ST1EPhaseActionsRule(_actionsEnvironment).applyRule();
        new ST1EAffiliationAttackRestrictionsRule(_actionsEnvironment).applyRule();
        new ST1EChangeAffiliationRule(_actionsEnvironment).applyRule();
//        new ActivatePhaseActionsRule(_actionsEnvironment).applyRule();
    }
}