package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.rules.generic.RuleSet;

public class TribblesRuleSet extends RuleSet<TribblesGame> {

    @Override
    protected void applySpecificRules(TribblesGame cardGame) {
        applyActionProxiesAsRules(cardGame, new TribblesPlayCardRule());
    }

}