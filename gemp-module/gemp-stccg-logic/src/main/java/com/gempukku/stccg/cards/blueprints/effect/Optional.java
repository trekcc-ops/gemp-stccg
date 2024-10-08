package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.decisions.YesNoDecision;

import java.util.List;

public class Optional implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "player", "text", "effect");

        final String text = node.get("text").textValue();

        if (text == null)
            throw new InvalidCardDefinitionException("There is a text required for optional effects");

        final PlayerSource playerSource =
                PlayerResolver.resolvePlayer(BlueprintUtils.getString(node, "player", "you"));
        final List<EffectBlueprint> effectBlueprints = environment.getEffectAppendersFromJSON(node.get("effect"));

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String choosingPlayer = playerSource.getPlayerId(context);
                SubAction subAction = action.createSubAction();
                subAction.appendCost(
                        new PlayOutDecisionEffect(context.getGame(), choosingPlayer,
                        new YesNoDecision(context.substituteText(_text)) {
                            @Override
                            protected void yes() {
                                effectBlueprints.forEach(effectAppender -> effectAppender.addEffectToAction(
                                                cost, subAction, context.createDelegateContext(choosingPlayer)));
                            }
                        }));
                return new StackActionEffect(context.getGame(), subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayerId(actionContext);
                for (EffectBlueprint effectBlueprint : effectBlueprints) {
                    if (!effectBlueprint.isPlayableInFull(actionContext.createDelegateContext(choosingPlayer)))
                        return false;
                }

                return true;
            }

            @Override
            public boolean isPlayabilityCheckedForEffect() {
                return effectBlueprints.stream().anyMatch(EffectBlueprint::isPlayabilityCheckedForEffect);
            }
        };
    }
}