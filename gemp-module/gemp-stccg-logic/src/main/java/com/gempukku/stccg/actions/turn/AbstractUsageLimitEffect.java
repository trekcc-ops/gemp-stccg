package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;

public abstract class AbstractUsageLimitEffect extends DefaultEffect implements UsageEffect {
    /**
     * Creates an effect that can be added to an action as a usage cost.
     * @param action the action performing this effect
     */
    protected AbstractUsageLimitEffect(Action action) {
        super(action);
    }

    public boolean isPlayableInFull() { return true; }
}