package com.gempukku.lotro.rules;

import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.rules.lotronly.*;

public class RuleSet {
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final ModifiersLogic _modifiersLogic;

    public RuleSet(DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        _actionsEnvironment = actionsEnvironment;
        _modifiersLogic = modifiersLogic;
    }

    public void applyRuleSet() {
        new EnduringRule(_modifiersLogic).applyRule();
        new HunterRule(_modifiersLogic).applyRule();

        new DiscardedCardRule(_actionsEnvironment).applyRule();

        new KilledCardRule(_actionsEnvironment).applyRule();

        new TransferItemRule(_actionsEnvironment).applyRule();

        new StatModifiersRule(_modifiersLogic).applyRule();

        new FollowerRule(_actionsEnvironment).applyRule();

        new PlayCardInPhaseRule(_actionsEnvironment).applyRule();
        new PlayResponseEventRule(_actionsEnvironment).applyRule();

        new ActivateResponseAbilitiesRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromHandRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromDiscardRule(_actionsEnvironment).applyRule();
        new ActivatePhaseActionsFromStackedRule(_actionsEnvironment).applyRule();

        new RequiredTriggersRule(_actionsEnvironment).applyRule();
        new OptionalTriggersRule(_actionsEnvironment).applyRule();
        new OptionalTriggersFromHandRule(_actionsEnvironment).applyRule();

        new HealByDiscardRule(_actionsEnvironment).applyRule();

        new TakeOffRingRule(_actionsEnvironment).applyRule();
        new ConcealedRule(_actionsEnvironment).applyRule();
    }
}
