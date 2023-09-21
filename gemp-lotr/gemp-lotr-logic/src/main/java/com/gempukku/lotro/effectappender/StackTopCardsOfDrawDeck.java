package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.PlayerSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.CardResolver;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.effects.DoNothingEffect;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.StackTopCardsFromDeckEffect;
import org.json.simple.JSONObject;

public class StackTopCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "deck", "where", "count");

        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");
        final String where = FieldUtils.getString(effectObject.get("where"), "where");
        final int count = FieldUtils.getInteger(effectObject.get("count"), "count", 1);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);

        MultiEffectAppender result = new MultiEffectAppender();
        String cardMemory = "_temp";

        result.addEffectAppender(
                CardResolver.resolveCard(where, cardMemory, "you", "Choose card to stack on", environment));
        result.addEffectAppender(
                new DelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final LotroPhysicalCard card = actionContext.getCardFromMemory(cardMemory);
                        if (card != null) {
                            final String deckId = playerSource.getPlayer(actionContext);

                            return new StackTopCardsFromDeckEffect(deckId, count, card);
                        } else
                            return new DoNothingEffect();
                    }
                });

        return result;
    }

}
