package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.rules.tribbles.TribblesOptionalTriggersRule;

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