package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.turn.IncrementTurnLimitEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.effect.DelayedEffectBlueprint;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprintDeserializer;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.JsonUtils;

import java.util.LinkedList;
import java.util.List;

public abstract class DefaultActionSource implements ActionSource {
    private final List<Requirement> requirements = new LinkedList<>();

    protected final List<EffectBlueprint> costs = new LinkedList<>();
    protected final List<EffectBlueprint> effects = new LinkedList<>();

    protected String _text;

    public void setText(String text) {
        this._text = text;
    }

    public void addRequirement(Requirement requirement) {
        this.requirements.add(requirement);
    }

    public void addCost(EffectBlueprint effectBlueprint) {
        costs.add(effectBlueprint);
    }

    public void addEffect(EffectBlueprint effectBlueprint) {
        effects.add(effectBlueprint);
    }

    @Override
    public boolean isValid(ActionContext actionContext) {
        return actionContext.acceptsAllRequirements(requirements);
    }

    @Override
    public void appendActionToContext(Action action, ActionContext actionContext) {
        if (_text != null)
            action.setText(actionContext.substituteText(_text));

        costs.forEach(cost -> cost.addEffectToAction(true, action, actionContext));

        effects.forEach(actionEffect -> actionEffect.addEffectToAction(false, action, actionContext));
    }

    public void processRequirementsCostsAndEffects(JsonNode node)
            throws InvalidCardDefinitionException {

        if (!node.has("cost") && !node.has("effect"))
            throw new InvalidCardDefinitionException("Action does not contain a cost, nor effect");

        if (node.has("requires")) {
            for (JsonNode requirement : JsonUtils.toArray(node.get("requires")))
                    addRequirement(RequirementFactory.getRequirement(requirement));
        }

        if (node.has("cost")) {
            for (JsonNode cost : JsonUtils.toArray(node.get("cost"))) {
                final EffectBlueprint effectBlueprint = EffectBlueprintDeserializer.getEffectBlueprint(cost);
                addRequirement(effectBlueprint::isPlayableInFull);
                addCost(effectBlueprint);
            }
        }

        if (node.has("effect")) {
            for (JsonNode effect : JsonUtils.toArray(node.get("effect"))) {
                final EffectBlueprint effectBlueprint = EffectBlueprintDeserializer.getEffectBlueprint(effect);
                if (effectBlueprint.isPlayabilityCheckedForEffect())
                    addRequirement(effectBlueprint::isPlayableInFull);
                addEffect(effectBlueprint);
            }
        }
    }


    protected abstract Action createActionAndAppendToContext(PhysicalCard card, ActionContext context);

    public void setTurnLimit(int limitPerTurn) {
        addRequirement((actionContext) ->
                actionContext.getSource().checkTurnLimit(actionContext.getGame(), limitPerTurn));
        addCost(
            new DelayedEffectBlueprint() {
                @Override
                protected Effect createEffect(Action action, ActionContext actionContext) {
                    return new IncrementTurnLimitEffect(actionContext, limitPerTurn);
                }
            });
    }

    public Action createActionWithNewContext(PhysicalCard card) {
        return createActionAndAppendToContext(card,
                new DefaultActionContext(card.getOwnerName(), card, null, null));
    }

    public Action createActionWithNewContext(PhysicalCard card, EffectResult effectResult) {
        return createActionAndAppendToContext(card,
                new DefaultActionContext(card.getOwnerName(), card, null, effectResult));
    }


    public Action createActionWithNewContext(PhysicalCard card, Effect effect, EffectResult effectResult) {
        return createActionAndAppendToContext(card,
                new DefaultActionContext(card.getOwnerName(), card, effect, effectResult));
    }

    public Action createActionWithNewContext(PhysicalCard card, String playerId, Effect effect,
                                             EffectResult effectResult) {
        return createActionAndAppendToContext(card, new DefaultActionContext(playerId, card, effect, effectResult));
    }
}