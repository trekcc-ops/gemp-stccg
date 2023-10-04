package com.gempukku.lotro.rules;

import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.rules.tribbles.TribblesOptionalTriggersRule;

public class ST1ERuleSet extends RuleSet {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public ST1ERuleSet(DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        super(actionsEnvironment, modifiersLogic);
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRuleSet() {
        new DiscardedCardRule(_actionsEnvironment).applyRule();
        new ST1EPlayCardInPhaseRule(_actionsEnvironment).applyRule();
        new TribblesOptionalTriggersRule(_actionsEnvironment).applyRule();
        new ActivateResponseAbilitiesRule(_actionsEnvironment).applyRule();
        new RequiredTriggersRule(_actionsEnvironment).applyRule();
    }
}