package com.gempukku.lotro.rules.tribbles;

import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.rules.*;

public class TribblesRuleSet extends RuleSet {
    private final DefaultActionsEnvironment _actionsEnvironment;

    public TribblesRuleSet(DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        super(actionsEnvironment, modifiersLogic);
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRuleSet() {
        new DiscardedCardRule(_actionsEnvironment).applyRule();
        new TribblesPlayCardRule(_actionsEnvironment).applyRule();
        new TribblesOptionalTriggersRule(_actionsEnvironment).applyRule();
        new ActivateResponseAbilitiesRule(_actionsEnvironment).applyRule();
        new RequiredTriggersRule(_actionsEnvironment).applyRule();
    }
}
