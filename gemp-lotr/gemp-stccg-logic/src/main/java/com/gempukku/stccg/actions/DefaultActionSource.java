package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import com.gempukku.stccg.rules.GameUtils;

import java.util.LinkedList;
import java.util.List;

public class DefaultActionSource implements ActionSource {
    private final List<Requirement> requirements = new LinkedList<>();

    private final List<EffectAppender> costs = new LinkedList<>();
    private final List<EffectAppender> effects = new LinkedList<>();

    private boolean requiresRanger;
    private String text;

    public void setRequiresRanger(boolean requiresRanger) {
        this.requiresRanger = requiresRanger;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void addPlayRequirement(Requirement requirement) {
        this.requirements.add(requirement);
    }

    public void addCost(EffectAppender effectAppender) {
        costs.add(effectAppender);
    }

    public void addEffect(EffectAppender effectAppender) {
        effects.add(effectAppender);
    }

    @Override
    public boolean requiresRanger() {
        return requiresRanger;
    }

    @Override
    public boolean isValid(ActionContext actionContext) {
        return RequirementUtils.acceptsAllRequirements(requirements, actionContext);
    }

    @Override
    public void createAction(CostToEffectAction action, ActionContext actionContext) {
        if (text != null)
            action.setText(GameUtils.SubstituteText(text, actionContext));

        costs.forEach(cost -> cost.appendEffect(true, action, actionContext));

        effects.forEach(actionEffect -> actionEffect.appendEffect(false, action, actionContext));
    }
}
