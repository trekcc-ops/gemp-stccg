package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.gamestate.ActionProxy;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.game.DefaultGame;

public class RuleSet {
    private final ActionsEnvironment _actionsEnvironment;
    public RuleSet(DefaultGame game) {
        _actionsEnvironment = game.getActionsEnvironment();
    }

    private void applyGenericRules(DefaultGame cardGame) {
        // TODO - I see these less as "rules" and more as basic turn loop mechanisms
        applyActionProxiesAsRules(
                new RequiredTriggersRule(cardGame),
                new DiscardedCardRule(cardGame),
                new OptionalTriggersRule(cardGame),
                new ActivatePhaseActionsRule(cardGame),
                new ActivateResponseAbilitiesRule(cardGame) // Less sure about this one
        );
    }

    protected void applySpecificRules() { }

    public void applyRuleSet(DefaultGame cardGame) {
        applyGenericRules(cardGame);
        applySpecificRules();
    }

    protected void applyActionProxiesAsRules(ActionProxy... rules) {
        for (ActionProxy rule : rules) {
            _actionsEnvironment.addAlwaysOnActionProxy(rule);
        }
    }
}