package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.TimeResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Keyword;
import com.gempukku.stccg.effects.AddUntilModifierEffect;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.RemoveKeywordModifier;
import org.json.simple.JSONObject;

import java.util.Collection;

public class RemoveKeyword implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "memorize", "keyword", "until");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        Keyword keyword = FieldUtils.getEnum(Keyword.class, effectObject.get("keyword"), "keyword");
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter, valueSource, memory, "you", "Choose cards to remove keyword from", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        return new AddUntilModifierEffect(
                                new RemoveKeywordModifier(actionContext.getSource(), Filters.in(cardsFromMemory), keyword), until);
                    }
                });

        return result;
    }

}
