package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.CostToEffectAction;

public class StackActionEffect implements Effect {
    private final CostToEffectAction _action;
    private final DefaultGame _game;

    public StackActionEffect(DefaultGame game, CostToEffectAction action) {
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
        return _action.getActionSource();
    }

    @Override
    public String getPerformingPlayer() {
        return _action.getPerformingPlayer();
    }

    @Override
    public boolean wasCarriedOut() {
        return _action.wasCarriedOut();
    }
}
