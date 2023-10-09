package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.ShuffleCardsIntoDrawDeckEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
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

                        final Collection<PhysicalCard> cardsInDiscard = actionContext.getCardsFromMemory("_temp");
//                        return new ShuffleCardsFromDiscardIntoDeckEffect(actionContext.getSource(), actionContext.getPerformingPlayer(), cardsInDiscard);

                                // Added
                        return new ShuffleCardsIntoDrawDeckEffect(actionContext.getSource(), Zone.DISCARD, recyclePlayer, cardsInDiscard);
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