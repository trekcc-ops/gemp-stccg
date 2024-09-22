package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.rules.generic.RuleSet;

public class TribblesRuleSet extends RuleSet {
    private final TribblesGame _game;

    public TribblesRuleSet(TribblesGame game) {
        super(game);
        _game = game;
    }

    @Override
    protected void applySpecificRules() {
        applyActionProxiesAsRules(new TribblesPlayCardRule(_game));
    }
}