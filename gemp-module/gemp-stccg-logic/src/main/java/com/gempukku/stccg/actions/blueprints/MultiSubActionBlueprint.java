package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.cards.ActionContext;

import java.util.LinkedList;
import java.util.List;

public class MultiSubActionBlueprint implements SubActionBlueprint {
    protected boolean playabilityCheckedForEffect;

    private final List<SubActionBlueprint> subActionBlueprints = new LinkedList<>();

    public void setPlayabilityCheckedForEffect(boolean playabilityCheckedForEffect) {
        this.playabilityCheckedForEffect = playabilityCheckedForEffect;
    }

    public void addEffectBlueprint(SubActionBlueprint subActionBlueprint) {
        subActionBlueprints.add(subActionBlueprint);
    }

    @Override
    public void addEffectToAction(boolean cost, CardPerformedAction action, ActionContext actionContext) {
        for (SubActionBlueprint subActionBlueprint : subActionBlueprints)
            subActionBlueprint.addEffectToAction(cost, action, actionContext);
    }

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        for (SubActionBlueprint subActionBlueprint : subActionBlueprints) {
            if (!subActionBlueprint.isPlayableInFull(actionContext))
                return false;
        }

        return true;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return playabilityCheckedForEffect;
    }

}