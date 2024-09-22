package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.rules.generic.RuleSet;

public class ST1ERuleSet extends RuleSet {
    private final ST1EGame _game;

    public ST1ERuleSet(ST1EGame game) {
        super(game);
        _game = game;
    }
    
    protected void applySpecificRules() {
        applyActionProxiesAsRules(
                new ST1EPlayCardInPhaseRule(_game),
                new ST1EChangeAffiliationRule(_game),
                new ST1EPhaseActionsRule(_game)
        );

        // TODO - This rule isn't an action proxy as of 9/22/24. Ideally this would be corrected.
        new ST1EAffiliationAttackRestrictionsRule(_game).applyRule();
    }
}