package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.game.DefaultGame;

public abstract class AbstractUsageLimitEffect extends DefaultEffect implements UsageEffect {

    protected final DefaultGame _game;
    /**
     * Creates an effect that can be added to an action as a usage cost.
     * @param action the action performing this effect
     */
    protected AbstractUsageLimitEffect(Action action) {
        super(action);
        _game = action.getGame();
    }

    public boolean isPlayableInFull() { return true; }
    public DefaultGame getGame() { return _game; }
}