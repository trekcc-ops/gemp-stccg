package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class PutCardsFromHandOnTopOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "selectingPlayer", "targetPlayer", "optional", "filter", "count", "reveal");

        final boolean reveal = environment.getBoolean(effectObject, "reveal", true);

        MultiEffectAppender result = new MultiEffectAppender();
        EffectAppender targetCardAppender = environment.buildTargetCardAppender(effectObject,
                "Choose cards from hand", Zone.HAND, "_temp", true);

        result.addEffectAppender(targetCardAppender);
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory("_temp");
                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard card : cards) {
                            result.add(new PutCardsFromZoneOnEndOfPileEffect(actionContext.getGame(), reveal, Zone.HAND, Zone.DRAW_DECK, EndOfPile.TOP, card));
                        }
                        return result;
                    }
                });

        return result;
    }

}
