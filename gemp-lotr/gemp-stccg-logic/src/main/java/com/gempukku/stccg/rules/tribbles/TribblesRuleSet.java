package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.rules.ActivateResponseAbilitiesRule;
import com.gempukku.stccg.rules.DiscardedCardRule;
import com.gempukku.stccg.rules.RequiredTriggersRule;
import com.gempukku.stccg.rules.RuleSet;

public class TribblesRuleSet extends RuleSet {
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final TribblesGame _game;

    public TribblesRuleSet(DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic, TribblesGame game) {
        super(actionsEnvironment, modifiersLogic);
        _actionsEnvironment = actionsEnvironment;
        _game = game;
    }

    public void applyRuleSet() {
        new DiscardedCardRule(_actionsEnvironment).applyRule();
        new TribblesPlayCardRule(_actionsEnvironment, _game).applyRule();
        new TribblesOptionalTriggersRule(_actionsEnvironment).applyRule();
        new ActivateResponseAbilitiesRule(_actionsEnvironment).applyRule();
        new RequiredTriggersRule(_actionsEnvironment).applyRule();
    }
}
