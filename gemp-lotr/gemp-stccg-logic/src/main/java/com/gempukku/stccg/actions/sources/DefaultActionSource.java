package com.gempukku.stccg.actions.sources;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.RequirementUtils;
import com.gempukku.stccg.rules.GameUtils;

import java.util.LinkedList;
import java.util.List;

public class DefaultActionSource implements ActionSource {
    private final List<Requirement> requirements = new LinkedList<>();

    protected final List<EffectAppender> costs = new LinkedList<>();
    protected final List<EffectAppender> effects = new LinkedList<>();

    private boolean requiresRanger;
    protected String _text;

    public void setRequiresRanger(boolean requiresRanger) {
        this.requiresRanger = requiresRanger;
    }

    public void setText(String text) {
        this._text = text;
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
    public void appendActionToContext(CostToEffectAction action, ActionContext actionContext) {
        if (_text != null)
            action.setText(actionContext.substituteText(_text));

        costs.forEach(cost -> cost.appendEffect(true, action, actionContext));

        effects.forEach(actionEffect -> actionEffect.appendEffect(false, action, actionContext));
    }

    public Action createAction(PhysicalCard card) {
        return null;
        // TODO - This class should eventually be made abstract so that this method can be defined differently for different types of ActionSources
    }

    @Override
    public Action createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        return null;
        // TODO - This class should eventually be made abstract so that this method can be defined differently for different types of ActionSources
    }

}
