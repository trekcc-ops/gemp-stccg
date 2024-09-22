package com.gempukku.stccg.rules.generic;

import com.gempukku.stccg.actions.ActionProxy;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.game.DefaultGame;

public class RuleSet {
    private final ActionsEnvironment _actionsEnvironment;
    public RuleSet(DefaultGame game) { _actionsEnvironment = game.getActionsEnvironment(); }

    private void applyGenericRules() {
        DefaultGame game = _actionsEnvironment.getGame();
        // TODO - I see these less as "rules" and more as basic turn loop mechanisms
        applyActionProxiesAsRules(
                new RequiredTriggersRule(game),
                new DiscardedCardRule(game),
                new OptionalTriggersRule(game),
                new ActivatePhaseActionsFromHandRule(game),
                new ActivatePhaseActionsRule(game),
                new ActivateResponseAbilitiesRule(game) // Less sure about this one
        );
    }

    protected void applySpecificRules() { }

    public void applyRuleSet() {
        applyGenericRules();
        applySpecificRules();
    }

    protected void applyActionProxiesAsRules(ActionProxy... rules) {
        for (ActionProxy rule : rules) {
            _actionsEnvironment.addAlwaysOnActionProxy(rule);
        }
    }
}