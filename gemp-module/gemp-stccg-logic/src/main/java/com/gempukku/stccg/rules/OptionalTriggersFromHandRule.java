package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AbstractActionProxy;
import com.gempukku.stccg.actions.ActionsEnvironment;

public class OptionalTriggersFromHandRule {
    private final ActionsEnvironment actionsEnvironment;

    public OptionalTriggersFromHandRule(ActionsEnvironment actionsEnvironment) {
        this.actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                });
    }
}
