package com.gempukku.stccg.cards.blueprints.effectappender.memorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.effectappender.DefaultDelayedAppender;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppender;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;
import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import com.gempukku.stccg.actions.Effect;

public class ChooseANumber implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "text", "amount", "memorize");
        // TODO - Removed "to" and "from" here. Implement a choose option in ValueResolver to take one parameter here.

        final String displayText =
                environment.getString(effectObject, "text", "Choose a number");
        final ValueSource valueSource =
                ValueResolver.resolveEvaluator(effectObject.get("from"), 0, environment);

        final String memorize = effectObject.get("memorize").textValue();

        if (memorize == null)
            throw new InvalidCardDefinitionException("ChooseANumber requires a field to memorize the value");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new PlayOutDecisionEffect(context.getGame(), context.getPerformingPlayerId(),
                    new IntegerAwaitingDecision(1, context.substituteText(displayText),
                        valueSource.getMinimum(context),
                        valueSource.getMaximum(context))
                {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {
                        context.setValueToMemory(memorize, String.valueOf(getValidatedResult(result)));
                    }
                });
            }
        };
    }
}