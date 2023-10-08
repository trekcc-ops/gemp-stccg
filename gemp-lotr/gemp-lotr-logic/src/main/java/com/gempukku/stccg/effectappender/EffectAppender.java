package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.game.DefaultGame;

public interface EffectAppender<AbstractGame extends DefaultGame> {
    void appendEffect(boolean cost, CostToEffectAction action, DefaultActionContext<AbstractGame> actionContext);

    boolean isPlayableInFull(DefaultActionContext<AbstractGame> actionContext);

    default boolean isPlayabilityCheckedForEffect() {
        return false;
    }
}
