package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.decisions.YesNoDecision;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import com.gempukku.stccg.actions.StackActionEffect;

import java.util.List;

public class Optional implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "player", "text", "effect");

        final String text = node.get("text").textValue();

        if (text == null)
            throw new InvalidCardDefinitionException("There is a text required for optional effects");

        final PlayerSource playerSource =
                PlayerResolver.resolvePlayer(environment.getString(node, "player", "you"));
        final List<EffectAppender> effectAppenders = environment.getEffectAppendersFromJSON(node.get("effect"));

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
                                effectAppenders.forEach(effectAppender -> effectAppender.appendEffect(
                                                cost, subAction, context.createDelegateContext(choosingPlayer)));
                            }
                        }));
                return new StackActionEffect(context.getGame(), subAction);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String choosingPlayer = playerSource.getPlayerId(actionContext);
                for (EffectAppender effectAppender : effectAppenders) {
                    if (!effectAppender.isPlayableInFull(actionContext.createDelegateContext(choosingPlayer)))
                        return false;
                }

                return true;
            }

            @Override
            public boolean isPlayabilityCheckedForEffect() {
                return effectAppenders.stream().anyMatch(EffectAppender::isPlayabilityCheckedForEffect);
            }
        };
    }
}