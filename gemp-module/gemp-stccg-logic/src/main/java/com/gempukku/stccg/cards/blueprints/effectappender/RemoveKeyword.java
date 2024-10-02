package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.TimeResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.AddUntilModifierEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.RemoveKeywordModifier;

import java.util.Collection;

public class RemoveKeyword implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "filter", "memorize", "keyword", "until");

        final ValueSource valueSource =
                ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = effectObject.get("filter").textValue();
        final String memory = environment.getString(effectObject, "memorize", "_temp");
        Keyword keyword = environment.getEnum(Keyword.class, effectObject.get("keyword").textValue(), "keyword");
        final TimeResolver.Time until = TimeResolver.resolveTime(effectObject.get("until"), "end(current)");

        MultiEffectAppender result = new MultiEffectAppender();
        PlayerSource you = ActionContext::getPerformingPlayerId;
        FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);

        result.addEffectAppender(
                CardResolver.resolveCardsInPlay(filter, valueSource, memory, you,
                        "Choose cards to remove keyword from", cardFilter));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = context.getCardsFromMemory(memory);
                        return new AddUntilModifierEffect(context.getGame(),
                                new RemoveKeywordModifier(context.getSource(),
                                        Filters.in(cardsFromMemory), keyword), until);
                    }
                });

        return result;
    }

}
