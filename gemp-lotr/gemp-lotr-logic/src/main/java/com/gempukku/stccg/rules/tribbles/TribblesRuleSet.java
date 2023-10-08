package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.rules.*;

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
