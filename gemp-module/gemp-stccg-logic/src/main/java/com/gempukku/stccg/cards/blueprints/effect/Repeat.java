package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

public class Repeat implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "amount", "effect");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(node.get("amount"));
        final EffectBlueprint effectBlueprint =
                environment.getEffectAppenderFactory().getEffectAppender(node.get("effect"));

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final int count = amountSource.evaluateExpression(context, null);
                if (count > 0) {
                    SubAction subAction = action.createSubAction();
                    for (int i = 0; i < count; i++)
                        effectBlueprint.addEffectToAction(cost, subAction, context);
                    return new StackActionEffect(context.getGame(), subAction);
                } else {
                    return null;
                }
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return effectBlueprint.isPlayableInFull(actionContext);
            }

            @Override
            public boolean isPlayabilityCheckedForEffect() {
                return effectBlueprint.isPlayabilityCheckedForEffect();
            }
        };
    }
}