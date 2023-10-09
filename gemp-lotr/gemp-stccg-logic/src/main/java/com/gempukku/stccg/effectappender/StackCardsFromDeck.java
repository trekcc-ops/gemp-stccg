package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.ShuffleDeckEffect;
import com.gempukku.stccg.effects.StackCardFromDeckEffect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StackCardsFromDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "filter", "where", "count", "shuffle");

        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String where = FieldUtils.getString(effectObject.get("where"), "where");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean shuffle = FieldUtils.getBoolean(effectObject.get("shuffle"), "shuffle", false);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCard(where, "_temp1", "you", "Choose card to stack on", environment));
        result.addEffectAppender(
                CardResolver.resolveCardsInDeck(filter, valueSource, "_temp2", "you", "Choose cards to stack", environment));
        result.addEffectAppender(
                new DelayedAppender() {
            @Override
            protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final PhysicalCard card = actionContext.getCardFromMemory("_temp1");
                if (card != null) {
                    final Collection<? extends PhysicalCard> cardsInDeck = actionContext.getCardsFromMemory("_temp2");

                    List<Effect> result = new LinkedList<>();
                    for (PhysicalCard physicalCard : cardsInDeck) {
                        result.add(new StackCardFromDeckEffect(physicalCard, card));
                    }

                    return result;
                }
                return null;
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
