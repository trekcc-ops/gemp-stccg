package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class MultiEffectAppender<AbstractGame extends DefaultGame> implements EffectAppender<AbstractGame> {
    private boolean playabilityCheckedForEffect;

    private final List<EffectAppender> effectAppenders = new LinkedList<>();

    public void setPlayabilityCheckedForEffect(boolean playabilityCheckedForEffect) {
        this.playabilityCheckedForEffect = playabilityCheckedForEffect;
    }

    public void addEffectAppender(EffectAppender effectAppender) {
        effectAppenders.add(effectAppender);
    }

    @Override
    public void appendEffect(boolean cost, CostToEffectAction action, DefaultActionContext<AbstractGame> actionContext) {
        for (EffectAppender effectAppender : effectAppenders)
            effectAppender.appendEffect(cost, action, actionContext);
    }

    @Override
    public boolean isPlayableInFull(DefaultActionContext<AbstractGame> actionContext) {
        for (EffectAppender effectAppender : effectAppenders) {
            if (!effectAppender.isPlayableInFull(actionContext))
                return false;
        }

        return true;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return playabilityCheckedForEffect;
    }
}
