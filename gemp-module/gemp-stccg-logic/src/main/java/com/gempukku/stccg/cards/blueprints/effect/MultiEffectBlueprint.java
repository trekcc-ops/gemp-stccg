package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.ActionContext;

import java.util.LinkedList;
import java.util.List;

public class MultiEffectBlueprint implements EffectBlueprint {
    private boolean playabilityCheckedForEffect;

    private final List<EffectBlueprint> effectBlueprints = new LinkedList<>();

    public void setPlayabilityCheckedForEffect(boolean playabilityCheckedForEffect) {
        this.playabilityCheckedForEffect = playabilityCheckedForEffect;
    }

    public void addEffectBlueprint(EffectBlueprint effectBlueprint) {
        effectBlueprints.add(effectBlueprint);
    }

    @Override
    public void addEffectToAction(boolean cost, Action action, ActionContext actionContext) {
        for (EffectBlueprint effectBlueprint : effectBlueprints)
            effectBlueprint.addEffectToAction(cost, action, actionContext);
    }

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        for (EffectBlueprint effectBlueprint : effectBlueprints) {
            if (!effectBlueprint.isPlayableInFull(actionContext))
                return false;
        }

        return true;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return playabilityCheckedForEffect;
    }

    public List<EffectBlueprint> getEffectBlueprints() { return effectBlueprints; }
}