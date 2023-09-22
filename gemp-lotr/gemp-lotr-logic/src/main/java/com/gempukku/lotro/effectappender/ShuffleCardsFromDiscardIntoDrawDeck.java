package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.CardResolver;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.effects.ShuffleCardsFromDiscardIntoDeckEffect;
import com.gempukku.lotro.effects.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class ShuffleCardsFromDiscardIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "count", "player");
//        FieldUtils.validateAllowedFields(effectObject, "filter", "count");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
            // Added the next 2 lines
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                    // player used to be "you"
//                CardResolver.resolveCardsInDiscard(filter, valueSource, "_temp", "you", "Choose cards to shuffle in", environment));
                CardResolver.resolveCardsInDiscard(filter, valueSource, "_temp", player, "Choose cards to shuffle in", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                                // Added
                        final String recyclePlayer = playerSource.getPlayer(actionContext);

                        final Collection<? extends LotroPhysicalCard> cardsInDiscard = actionContext.getCardsFromMemory("_temp");
//                        return new ShuffleCardsFromDiscardIntoDeckEffect(actionContext.getSource(), actionContext.getPerformingPlayer(), cardsInDiscard);

                                // Added
                        return new ShuffleCardsFromDiscardIntoDeckEffect(actionContext.getSource(), recyclePlayer, cardsInDiscard);
                    }
                });

/*
        protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
            final String recyclePlayer = playerSource.getPlayer(actionContext);
            final Collection<? extends LotroPhysicalCard> cardsInDiscard = actionContext.getCardsFromMemory("_temp");
            return new ShuffleCardsFromDiscardIntoDeckEffect(actionContext.getSource(), recyclePlayer, cardsInDiscard);
        }
*/
        return result;
    }

}