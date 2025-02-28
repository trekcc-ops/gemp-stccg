package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.ActionProxy;

public class RuleSet<T extends DefaultGame> {

    private void applyGenericRules(T cardGame) {
        applyActionProxiesAsRules(cardGame,
                new RequiredTriggersRule(cardGame),
                new DiscardedCardRule(cardGame),
                new OptionalTriggersRule(cardGame),
                new ActivatePhaseActionsRule(cardGame),
                new ActivateResponseAbilitiesRule(cardGame) // Less sure about this one
        );
    }

    protected void applySpecificRules(T cardGame) { }


    public void applyRuleSet(T cardGame) {
        applyGenericRules(cardGame);
        applySpecificRules(cardGame);
    }

    protected void applyActionProxiesAsRules(T cardGame, ActionProxy... rules) {
        for (ActionProxy rule : rules) {
            cardGame.getActionsEnvironment().addAlwaysOnActionProxy(rule);
        }
    }
}