package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.modifiers.ModifiersLogic;

public class RuleSet {
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final ModifiersLogic _modifiersLogic;

    public RuleSet(DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        _actionsEnvironment = actionsEnvironment;
        _modifiersLogic = modifiersLogic;
    }

    public void applyRuleSet() {
        new DiscardedCardRule(_actionsEnvironment).applyRule();

        new StatModifiersRule(_modifiersLogic).applyRule();

        new PlayCardInPhaseRule(_actionsEnvironment).applyRule();

        new ActivateResponseAbilitiesRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromHandRule(_actionsEnvironment).applyRule();

        new RequiredTriggersRule(_actionsEnvironment).applyRule();
        new OptionalTriggersRule(_actionsEnvironment).applyRule();
        new OptionalTriggersFromHandRule(_actionsEnvironment).applyRule();
    }
}
