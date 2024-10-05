package com.gempukku.stccg.actions.sources;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.IncrementPhaseLimitEffect;
import com.gempukku.stccg.actions.turn.IncrementTurnLimitEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effectappender.AbstractEffectAppender;
import com.gempukku.stccg.effectappender.EffectAppender;
import com.gempukku.stccg.effectappender.EffectAppenderFactory;
import com.gempukku.stccg.requirement.Requirement;

import java.util.LinkedList;
import java.util.List;

public class DefaultActionSource implements ActionSource {
    private final List<Requirement> requirements = new LinkedList<>();

    protected final List<EffectAppender> costs = new LinkedList<>();
    protected final List<EffectAppender> effects = new LinkedList<>();

    protected String _text;

    public void setText(String text) {
        this._text = text;
    }

    public void addRequirement(Requirement requirement) {
        this.requirements.add(requirement);
    }

    private void addCost(EffectAppender effectAppender) {
        costs.add(effectAppender);
    }

    private void addEffect(EffectAppender effectAppender) {
        effects.add(effectAppender);
    }

    public boolean isValid(ActionContext actionContext) {
        return actionContext.acceptsAllRequirements(requirements);
    }

    public void appendActionToContext(CostToEffectAction action, ActionContext actionContext) {
        if (_text != null)
            action.setText(actionContext.substituteText(_text));

        costs.forEach(cost -> cost.appendEffect(true, action, actionContext));

        effects.forEach(actionEffect -> actionEffect.appendEffect(false, action, actionContext));
    }

    @Override
    public Action createActionAndAppendToContext(PhysicalCard card, ActionContext actionContext) {
        return null;
        // TODO - This class should eventually be made abstract so that this method can be defined differently for different types of ActionSources
    }

    public void processRequirementsCostsAndEffects(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        if (!node.has("cost") && !node.has("effect"))
            throw new InvalidCardDefinitionException("Action does not contain a cost, nor effect");
        
        if (node.has("requires")) {
            if (node.get("requires").isArray()) {
                for (JsonNode requirement : node.get("requires")) {
                    addRequirement(environment.getRequirement(requirement));
                }
            } else addRequirement(environment.getRequirement(node.get("requires")));
        }

        final EffectAppenderFactory effectAppenderFactory = environment.getEffectAppenderFactory();

        if (node.has("cost")) {
            if (node.get("cost").isArray()) {
                for (JsonNode cost : node.get("cost")) {
                    final EffectAppender effectAppender = effectAppenderFactory.getEffectAppender(cost);
                    addRequirement(effectAppender::isPlayableInFull);
                    addCost(effectAppender);
                }
            } else {
                final EffectAppender effectAppender = effectAppenderFactory.getEffectAppender(node.get("cost"));
                addRequirement(effectAppender::isPlayableInFull);
                addCost(effectAppender);
            }
        }
        
        if (node.has("effect")) {
            if (node.get("effect").isArray()) {
                for (JsonNode effect : node.get("effect")) {
                    final EffectAppender effectAppender = effectAppenderFactory.getEffectAppender(effect);
                    if (effectAppender.isPlayabilityCheckedForEffect())
                        addRequirement(effectAppender::isPlayableInFull);
                    addEffect(effectAppender);
                }
            } else {
                final EffectAppender effectAppender = effectAppenderFactory.getEffectAppender(node.get("effect"));
                if (effectAppender.isPlayabilityCheckedForEffect())
                    addRequirement(effectAppender::isPlayableInFull);
                addEffect(effectAppender);
            }
        }
    }

    public void setPhaseLimit(Phase phase, int limitPerPhase) {
        addRequirement((actionContext) -> actionContext.getSource().checkPhaseLimit(phase, limitPerPhase));
        addCost(
                new AbstractEffectAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        return new IncrementPhaseLimitEffect(actionContext, phase, limitPerPhase);
                    }
                });
    }

    public void setTurnLimit(int limitPerTurn) {
        addRequirement((actionContext) -> actionContext.getSource().checkTurnLimit(limitPerTurn));
        addCost(
            new AbstractEffectAppender() {
                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                    return new IncrementTurnLimitEffect(actionContext, limitPerTurn);
                }
            });
    }
}