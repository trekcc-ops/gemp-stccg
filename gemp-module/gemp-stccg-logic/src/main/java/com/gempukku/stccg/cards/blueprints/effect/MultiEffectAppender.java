package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;

import java.util.LinkedList;
import java.util.List;

public class MultiEffectAppender implements EffectAppender {
    private boolean playabilityCheckedForEffect;

    private final List<EffectAppender> effectAppenders = new LinkedList<>();

    public void setPlayabilityCheckedForEffect(boolean playabilityCheckedForEffect) {
        this.playabilityCheckedForEffect = playabilityCheckedForEffect;
    }

    public void addEffectAppender(EffectAppender effectAppender) {
        effectAppenders.add(effectAppender);
    }

    @Override
    public void appendEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
        for (EffectAppender effectAppender : effectAppenders)
            effectAppender.appendEffect(cost, action, actionContext);
    }

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
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