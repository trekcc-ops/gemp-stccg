package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.PutCardFromZoneIntoHandEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PutCardsFromDiscardIntoHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "player", "discard", "memorize");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String discard = FieldUtils.getString(effectObject.get("discard"), "disard", player);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDiscard(filter, valueSource, memorize, player, discard, "Choose cards from discard", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cards = actionContext.getCardsFromMemory(memorize);
                        List<Effect> result = new LinkedList<>();
                        for (PhysicalCard card : cards) {
                            result.add(
                                    new PutCardFromZoneIntoHandEffect(card, Zone.DISCARD));
                        }

                        return result;
                    }
                });

        return result;

    }
}
