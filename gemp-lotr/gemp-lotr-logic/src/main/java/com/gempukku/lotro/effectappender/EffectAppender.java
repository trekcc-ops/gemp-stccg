package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.DefaultActionContext;
import com.gempukku.lotro.game.DefaultGame;

public interface EffectAppender<AbstractGame extends DefaultGame> {
    void appendEffect(boolean cost, CostToEffectAction action, DefaultActionContext<AbstractGame> actionContext);

    boolean isPlayableInFull(DefaultActionContext<AbstractGame> actionContext);

    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}
