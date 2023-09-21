package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.CardResolver;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.effects.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DiscardFromPlay implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "count", "filter", "memorize", "memorizeStackedCards");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource discardingPlayer = PlayerResolver.resolvePlayer(player);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");
        final String stackedCardsMemory = FieldUtils.getString(effectObject.get("memorizeStackedCards"), "memorizeStackedCards");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter,
                        (actionContext) -> Filters.canBeDiscarded(actionContext.getPerformingPlayer(), actionContext.getSource()),
                        valueSource, memory, player, "Choose cards to discard", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final String discardingPlayerId = discardingPlayer.getPlayer(actionContext);
                        final Collection<? extends LotroPhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        if (stackedCardsMemory != null) {
                            List<LotroPhysicalCard> stackedCards = new LinkedList<>();
                            for (LotroPhysicalCard physicalCard : cardsFromMemory) {
                                stackedCards.addAll(actionContext.getGame().getGameState().getStackedCards(physicalCard));
                            }

                            actionContext.setCardMemory(stackedCardsMemory, stackedCards);
                        }
                        return new DiscardCardsFromPlayEffect(discardingPlayerId, actionContext.getSource(), Filters.in(cardsFromMemory));
                    }
                });

        return result;
    }

}
