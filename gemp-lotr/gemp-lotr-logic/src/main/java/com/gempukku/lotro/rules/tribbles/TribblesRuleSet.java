package com.gempukku.lotro.rules.tribbles;

import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.rules.ActivatePhaseActionsFromDiscardRule;
import com.gempukku.lotro.rules.ActivatePhaseActionsFromHandRule;
import com.gempukku.lotro.rules.ActivatePhaseActionsFromStackedRule;
import com.gempukku.lotro.rules.ActivatePhaseActionsRule;
import com.gempukku.lotro.rules.ActivateResponseAbilitiesRule;
import com.gempukku.lotro.rules.DiscardedCardRule;
import com.gempukku.lotro.rules.PlayResponseEventRule;
import com.gempukku.lotro.rules.RequiredTriggersRule;
import com.gempukku.lotro.rules.RuleSet;
import com.gempukku.lotro.rules.StatModifiersRule;

public class TribblesRuleSet extends RuleSet {
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final ModifiersLogic _modifiersLogic;

    public TribblesRuleSet(DefaultGame game, DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic) {
        super(game, actionsEnvironment, modifiersLogic);
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