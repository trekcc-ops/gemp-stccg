package com.gempukku.stccg.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.ShuffleCardsFromPlayIntoDeckEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;

import java.util.Collection;

public class ShuffleCardsFromPlayIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "filter", "count", "memorize");

        String player = environment.getString(effectObject, "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final String filter = environment.getString(effectObject, "filter", "choose(any)");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memorize = environment.getString(effectObject, "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter, valueSource, memorize, player, "Choose cards to shuffle into your deck", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<? extends PhysicalCard> cardsInPlay = context.getCardsFromMemory(memorize);

                        return new ShuffleCardsFromPlayIntoDeckEffect(context, playerSource.getPlayerId(context), cardsInPlay);
                    }
                });

        return result;
    }

}
