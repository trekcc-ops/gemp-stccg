package com.gempukku.stccg.effectappender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class PutCardsFromHandOnTopOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "hand", "optional", "filter", "count", "reveal");

        final String player = environment.getString(effectObject.get("player"), "player", "you");
        final String hand = environment.getString(effectObject.get("hand"), "hand", "you");
        final boolean optional = environment.getBoolean(effectObject.get("optional"), "optional", false);
        final String filter = environment.getString(effectObject.get("filter"), "filter", "choose(any)");
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean reveal = environment.getBoolean(effectObject.get("reveal"), "reveal", true);

        JsonNode countObj = effectObject.get("count");

        ValueSource valueSource;
        if (optional) {
            String countStr = "1";
            if(countObj != null && !Objects.equals(countObj.toString().replaceAll(" +", ""), "{}")) {
                countStr = countObj.toString();
            }
            try {
                JsonNode obj = new ObjectMapper()
                        .readTree("{ \"type\": \"range\", \"from\": 0, \"to\": " + countStr + " }");
                valueSource = ValueResolver.resolveEvaluator(obj, environment);
            } catch (JsonProcessingException e) {
                valueSource = ValueResolver.resolveEvaluator("0-1");
            }
        }
        else {
            valueSource = count;
        }

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(filter, valueSource, "_temp", player, hand, "Choose cards from hand", true, environment));
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
