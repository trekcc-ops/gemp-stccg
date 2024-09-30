package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
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

public class PutCardsFromHandOnBottomOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "optional", "filter", "count", "reveal");

        final boolean optional = environment.getBoolean(effectObject, "optional", false);
        final String filter = environment.getString(effectObject, "filter", "choose(any)");
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean reveal = environment.getBoolean(effectObject, "reveal", true);

        ValueSource valueSource;
        if (optional)
            valueSource = ValueResolver.resolveEvaluator("0-" + count);
        else
            valueSource = count;

        MultiEffectAppender result = new MultiEffectAppender();
        final PlayerSource playerSource =
                PlayerResolver.resolvePlayer(environment.getString(effectObject, "player", "you"));

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(filter, valueSource, "_temp", playerSource,
                        "Choose cards from hand to put beneath draw deck", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory("_temp");
                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard card : cards) {
                            result.add(new PutCardsFromZoneOnEndOfPileEffect(actionContext.getGame(), reveal, Zone.HAND, Zone.DRAW_DECK, EndOfPile.BOTTOM, card));
                        }
                        return result;
                    }
                });

        return result;
    }

}
