package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.TimeResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.effects.AddUntilModifierEffect;
import com.gempukku.stccg.modifiers.StrengthModifier;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.effects.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ModifyStrength implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "count", "filter", "until", "memorize");

        final ValueSource amountSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), environment);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final TimeResolver.Time time = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter, valueSource, memory, "you", "Choose cards to modify strength of", environment));
        result.addEffectAppender(
                new DelayedAppender<>() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        final Evaluator evaluator = amountSource.getEvaluator(actionContext);
                        final int amount = evaluator.evaluateExpression(actionContext.getGame(), actionContext.getSource());
                        return new AddUntilModifierEffect(
                                new StrengthModifier(actionContext.getSource(), Filters.in(cardsFromMemory), amount), time);
                    }
                });

        return result;
    }

}
