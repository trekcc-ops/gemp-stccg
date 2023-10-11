package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.effects.defaulteffect.RemoveCardsFromTheGameEffect;
import com.gempukku.stccg.effects.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class RemoveFromTheGame implements EffectAppenderProducer {
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
                        valueSource, memory, player, "Choose cards to remove from the game", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final String removingPlayerId = discardingPlayer.getPlayer(actionContext);
                        final Collection<? extends PhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);
                        if (stackedCardsMemory != null) {
                            List<PhysicalCard> stackedCards = new LinkedList<>();
                            for (PhysicalCard physicalCard : cardsFromMemory) {
                                stackedCards.addAll(actionContext.getGame().getGameState().getStackedCards(physicalCard));
                            }

                            actionContext.setCardMemory(stackedCardsMemory, stackedCards);
                        }
                        return new RemoveCardsFromTheGameEffect(actionContext.getGame(), removingPlayerId, actionContext.getSource(), cardsFromMemory);
                    }
                });

        return result;
    }

}
