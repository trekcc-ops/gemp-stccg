package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.ShuffleCardsIntoDrawDeckEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

import java.util.Collection;

public class ShuffleCardsFromHandIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "filter", "count", "memorize");

        final PlayerSource playerSource = environment.getPlayerSource(effectObject, "player", true);
        final String memorize = environment.getString(effectObject, "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();
        EffectAppender targetCardAppender = environment.buildTargetCardAppender(effectObject, "Choose cards to shuffle into the draw deck", Zone.HAND, memorize);

        result.addEffectAppender(targetCardAppender);
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<PhysicalCard> cardsFromHand = context.getCardsFromMemory(memorize);

                        return new ShuffleCardsIntoDrawDeckEffect(
                                context.getGame(), context.getSource(), Zone.HAND, playerSource.getPlayerId(context), cardsFromHand
                        );
                    }
                });

        return result;
    }

}
