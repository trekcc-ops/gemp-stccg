package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.PutCardsFromZoneOnEndOfPileEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class PutCardsFromHandOnTopOfDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "hand", "optional", "filter", "count", "reveal");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final boolean optional = FieldUtils.getBoolean(effectObject.get("optional"), "optional", false);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean reveal = FieldUtils.getBoolean(effectObject.get("reveal"), "reveal", true);

        var countObj = (JSONObject)effectObject.get("count");

        ValueSource valueSource;
        if (optional) {
            String countStr = "1";
            if(countObj != null && !Objects.equals(countObj.toJSONString().replaceAll(" +", ""), "{}")) {
                countStr = countObj.toJSONString();
            }
            try {
                var obj = new JSONParser().parse("{ \"type\": \"range\", \"from\": 0, \"to\": " + countStr + " }");
                valueSource = ValueResolver.resolveEvaluator(obj, environment);
            } catch (ParseException e) {
                valueSource = ValueResolver.resolveEvaluator("0-1", environment);
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
