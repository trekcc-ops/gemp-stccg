package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.rules.*;

public class TribblesRuleSet extends RuleSet {
    private final ActionsEnvironment _actionsEnvironment;
    private final TribblesGame _game;

    public TribblesRuleSet(ActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic, TribblesGame game) {
        super(actionsEnvironment);
        _actionsEnvironment = actionsEnvironment;
        _game = game;
    }

    @Override
    public void applySpecificRules() {
        new TribblesPlayCardRule(_actionsEnvironment, _game).applyRule();
    }
}