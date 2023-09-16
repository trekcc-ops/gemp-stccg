package com.gempukku.lotro.rules.tribbles;

import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.rules.*;

public class TribblesRuleSet extends RuleSet {
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final ModifiersLogic _modifiersLogic;

    public TribblesRuleSet(DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        super(actionsEnvironment, modifiersLogic);
        _actionsEnvironment = actionsEnvironment;
        _modifiersLogic = modifiersLogic;
    }

    public void applyRuleSet() {
        new DiscardedCardRule(_actionsEnvironment).applyRule();

        new StatModifiersRule(_modifiersLogic).applyRule();

        new TribblesPlayCardRule(_actionsEnvironment).applyRule();
        new TribblesOptionalTriggersRule(_actionsEnvironment).applyRule();

        new PlayResponseEventRule(_actionsEnvironment).applyRule();

        new ActivateResponseAbilitiesRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromHandRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromDiscardRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromStackedRule(_actionsEnvironment).applyRule();

        new RequiredTriggersRule(_actionsEnvironment).applyRule();
    }
}
