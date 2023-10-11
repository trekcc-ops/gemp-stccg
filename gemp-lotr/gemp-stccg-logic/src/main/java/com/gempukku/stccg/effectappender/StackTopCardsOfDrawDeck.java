package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.StackTopCardsFromDeckEffect;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.DoNothingEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
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
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                        final PhysicalCard card = actionContext.getCardFromMemory(cardMemory);
                        if (card != null) {
                            final String deckId = playerSource.getPlayer(actionContext);

                            return new StackTopCardsFromDeckEffect(actionContext.getGame(), deckId, count, card);
                        } else
                            return new DoNothingEffect();
                    }
                });

        return result;
    }

}
