package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.ReturnCardsToHandEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ReturnToHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "count", "player");

        final String filter = effectObject.get("filter").textValue();
        final ValueSource valueSource =
                ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final PlayerSource player = environment.getPlayerSource(effectObject, "player", true);

        MultiEffectAppender result = new MultiEffectAppender();
        FilterableSource cardFilter = environment.getCardFilterableIfChooseOrAll(filter);
        Function<ActionContext, List<PhysicalCard>> cardSource =
                actionContext -> Filters.filterActive(actionContext.getGame(), Filters.any).stream().toList();

        result.addEffectAppender(
                CardResolver.resolveCardsInPlay(filter, actionContext -> (Filter) (game, physicalCard) ->
                                game.getModifiersQuerying().canBeReturnedToHand(physicalCard, actionContext.getSource()),
                        valueSource, "_temp", player, "Choose cards to return to hand", cardFilter,
                        cardSource));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<? extends PhysicalCard> cardsFromMemory = context.getCardsFromMemory("_temp");
                        return new ReturnCardsToHandEffect(
                                context.getGame(), context.getSource(), Filters.in(cardsFromMemory));
                    }
                });

        return result;
    }

}
