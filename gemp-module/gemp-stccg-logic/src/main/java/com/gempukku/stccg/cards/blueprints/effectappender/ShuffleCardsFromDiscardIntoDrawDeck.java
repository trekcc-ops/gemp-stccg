package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.ShuffleCardsIntoDrawDeckEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

import java.util.Collection;

public class ShuffleCardsFromDiscardIntoDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "filter", "count", "player");

        final PlayerSource playerSource = environment.getPlayerSource(effectObject, "player", true);

        MultiEffectAppender result = new MultiEffectAppender();

        EffectAppender targetCardAppender = environment.buildTargetCardAppender(effectObject, playerSource, "Choose cards to shuffle in", Zone.DISCARD, "_temp");

        result.addEffectAppender(targetCardAppender);
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final String recyclePlayer = playerSource.getPlayerId(context);
                        final Collection<PhysicalCard> cardsInDiscard = context.getCardsFromMemory("_temp");
                        return new ShuffleCardsIntoDrawDeckEffect(context.getGame(), context.getSource(), Zone.DISCARD, recyclePlayer, cardsInDiscard);
                    }
                });
        return result;
    }

}