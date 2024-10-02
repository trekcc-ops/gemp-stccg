package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.revealcards.RevealCardsFromYourHandEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.Zone;

import java.util.Collection;

public class RevealCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "filter", "memorize", "player");

        final String memorize = environment.getString(effectObject, "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();

        EffectAppender targetCardAppender = environment.buildTargetCardAppender(
                effectObject, "Choose cards to reveal", Zone.HAND, memorize);
        result.addEffectAppender(targetCardAppender);

        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<? extends PhysicalCard> cardsToReveal = context.getCardsFromMemory(memorize);
                        return new RevealCardsFromYourHandEffect(context, cardsToReveal);
                    }
                });

        return result;
    }

}
