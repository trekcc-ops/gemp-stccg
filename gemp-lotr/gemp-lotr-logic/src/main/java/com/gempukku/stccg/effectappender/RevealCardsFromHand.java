package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.effects.RevealCardsFromYourHandEffect;
import com.gempukku.stccg.effects.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;

public class RevealCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "filter", "memorize", "hand");

        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String memorize = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);


        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCardsInHand(filter, countSource, memorize, hand, hand, "Choose cards to reveal", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends PhysicalCard> cardsToReveal = actionContext.getCardsFromMemory(memorize);
                        return new RevealCardsFromYourHandEffect(actionContext.getSource(), actionContext.getPerformingPlayer(), cardsToReveal);
                    }
                });

        return result;
    }

}
