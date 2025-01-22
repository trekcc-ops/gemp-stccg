package com.gempukku.stccg.cards.blueprints.actionsource;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
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
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.Collections;
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
    public void appendActionToContext(TopLevelSelectableAction action, ActionContext actionContext) {
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


    protected abstract TopLevelSelectableAction createActionAndAppendToContext(PhysicalCard card, ActionContext context);

    public void setTurnLimit(int limitPerTurn) {
        addRequirement((actionContext) ->
                actionContext.getSource().checkTurnLimit(actionContext.getGame(), limitPerTurn));
        addCost(
            new DelayedEffectBlueprint() {
                @Override
                protected List<Action> createActions(CardPerformedAction action, ActionContext actionContext)
                        throws PlayerNotFoundException {
                    Action usageLimitAction = new UseOncePerTurnAction(
                            action, action.getPerformingCard(), actionContext.getPerformingPlayer());
                    return Collections.singletonList(usageLimitAction);
                }
            });
    }

    public TopLevelSelectableAction createActionWithNewContext(PhysicalCard card) {
        return createActionAndAppendToContext(card,
                new DefaultActionContext(card.getOwnerName(), card, null));
    }

    public TopLevelSelectableAction createActionWithNewContext(PhysicalCard card, ActionResult actionResult) {
        return createActionAndAppendToContext(card,
                new DefaultActionContext(card.getOwnerName(), card, actionResult));
    }


    public TopLevelSelectableAction createActionWithNewContext(PhysicalCard card, String playerId, ActionResult actionResult) {
        return createActionAndAppendToContext(card, new DefaultActionContext(playerId, card, actionResult));
    }
}