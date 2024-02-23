package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.game.DefaultGame;

public class SubAction extends AbstractCostToEffectAction {
    private final Action _action;
    protected final DefaultGame _game;

    public SubAction(Action action, DefaultGame game) {
        _action = action;
        _game = game;
    }

    @Override
    public DefaultGame getGame() { return _game; }

    @Override
    public ActionType getActionType() {
        return _action.getActionType();
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _action.getActionAttachedToCard();
    }

    @Override
    public PhysicalCard getActionSource() {
        return _action.getActionSource();
    }

    @Override
    public String getPerformingPlayer() {
        return _action.getPerformingPlayer();
    }

    @Override
    public void setPerformingPlayer(String playerId) {
        _action.setPerformingPlayer(playerId);
    }

    @Override
    public String getText() {
        return _action.getText();
    }

    @Override
    public Effect nextEffect() {
        if (isCostFailed()) {
            return null;
        } else {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            return getNextEffect();
        }
    }
}
