package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class StackActionEffect implements Effect {
    private final Action _action;
    private final DefaultGame _game;

    public StackActionEffect(DefaultGame game, Action action) {
        _game = game;
        _action = action;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public EffectType getType() {
        return null;
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    public void playEffect() {
        _game.getActionsEnvironment().addActionToStack(_action);
    }

    @Override
    public PhysicalCard getSource() {
        return _action.getPerformingCard();
    }

    @Override
    public String getPerformingPlayerId() {
        return _action.getPerformingPlayerId();
    }

    @Override
    public boolean wasCarriedOut() {
        return _action.wasCarriedOut();
    }
    public DefaultGame getGame() { return _game; }
}