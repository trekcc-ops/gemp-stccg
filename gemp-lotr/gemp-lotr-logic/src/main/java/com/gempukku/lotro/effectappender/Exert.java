package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.CardResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.ExertCharactersEffect;
import com.gempukku.lotro.filters.Filters;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Exert implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "count", "times", "filter", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final ValueSource timesSource = ValueResolver.resolveEvaluator(effectObject.get("times"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter,
                        (actionContext) -> Filters.canExert(actionContext.getSource(), timesSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null)),
                        valueSource, memory, player, "Choose cards to exert", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends LotroPhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);

                        final int times = timesSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        List<Effect> result = new LinkedList<>();
                        for (int i = 0; i < times; i++)
                            result.add(new ExertCharactersEffect(action, actionContext.getSource(), cardsFromMemory.toArray(new LotroPhysicalCard[0])));
                        return result;
                    }
                });

        return result;
    }

}
