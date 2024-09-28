package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.DefaultGame;

public abstract class AbstractSubActionEffect implements Effect {
    private CostToEffectAction _subAction;
    protected final DefaultGame _game;

    protected AbstractSubActionEffect(DefaultGame game) {
        _game = game;
    }

    protected void processSubAction(DefaultGame game, CostToEffectAction subAction) {
        _subAction = subAction;
        game.getActionsEnvironment().addActionToStack(_subAction);
    }

    @Override
    public boolean wasCarriedOut() {
        return _subAction != null && _subAction.wasCarriedOut();
    }

    public String getPerformingPlayerId() {
        return _subAction.getPerformingPlayerId();
    }

    public DefaultGame getGame() { return _game; }
}
