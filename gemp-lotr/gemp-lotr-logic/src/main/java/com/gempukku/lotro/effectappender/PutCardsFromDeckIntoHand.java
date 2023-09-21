package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.CardResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.PutCardFromDeckIntoHandEffect;
import com.gempukku.lotro.effects.ShuffleDeckEffect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PutCardsFromDeckIntoHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "shuffle", "reveal");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final boolean shuffle = FieldUtils.getBoolean(effectObject.get("shuffle"), "shuffle", true);
        final boolean reveal = FieldUtils.getBoolean(effectObject.get("reveal"), "reveal", true);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInDeck(filter, valueSource, "_temp", "you", "Choose cards from deck", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends LotroPhysicalCard> cards = actionContext.getCardsFromMemory("_temp");
                        List<Effect> result = new LinkedList<>();
                        for (LotroPhysicalCard card : cards) {
                            result.add(
                                    new PutCardFromDeckIntoHandEffect(card, reveal));
                        }

                        return result;
                    }
                });
        if (shuffle)
            result.addEffectAppender(
                    new DelayedAppender() {
                @Override
                protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                    return new ShuffleDeckEffect(actionContext.getPerformingPlayer());
                }
            });

        return result;

    }
}
