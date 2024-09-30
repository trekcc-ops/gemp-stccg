package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromPlayEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DiscardFromPlay implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "count", "filter", "memorize", "memorizeStackedCards");

        final String player = environment.getString(effectObject.get("player"), "player", "you");
        final PlayerSource discardingPlayer =
                environment.getPlayerSource(effectObject, "player", true);
        final ValueSource valueSource =
                ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = effectObject.get("filter").textValue();
        final String memory = environment.getString(effectObject, "memorize", "_temp");
        final String stackedCardsMemory = environment.getString(effectObject, "memorizeStackedCards");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter,
                        (actionContext) -> Filters.canBeDiscarded(actionContext.getPerformingPlayerId(), actionContext.getSource()),
                        valueSource, memory, player, "Choose cards to discard", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final String discardingPlayerId = discardingPlayer.getPlayerId(context);
                        final Collection<? extends PhysicalCard> cardsFromMemory = context.getCardsFromMemory(memory);
                        if (stackedCardsMemory != null) {
                            List<PhysicalCard> stackedCards = new LinkedList<>();
                            for (PhysicalCard physicalCard : cardsFromMemory) {
                                stackedCards.addAll(physicalCard.getStackedCards());
                            }

                            context.setCardMemory(stackedCardsMemory, stackedCards);
                        }
                        return new DiscardCardsFromPlayEffect(context, discardingPlayerId, Filters.in(cardsFromMemory));
                    }
                });

        return result;
    }

}
