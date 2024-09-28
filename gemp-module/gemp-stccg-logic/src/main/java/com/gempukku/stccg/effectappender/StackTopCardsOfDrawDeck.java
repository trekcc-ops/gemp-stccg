package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.DoNothingEffect;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackTopCardsFromDeckEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import org.json.simple.JSONObject;

public class StackTopCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "deck", "where", "count");

        final String deckOwner = environment.getString(effectObject.get("deck"), "deck", "you");
        final String where = environment.getString(effectObject.get("where"), "where");
        final int count = environment.getInteger(effectObject.get("count"), "count", 1);

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deckOwner);

        MultiEffectAppender result = new MultiEffectAppender();
        String cardMemory = "_temp";

        result.addEffectAppender(CardResolver.resolveCard(
                        where, cardMemory, "you", "Choose card to stack on", environment));
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final PhysicalCard card = context.getCardFromMemory(cardMemory);
                        if (card != null) {
                            final String deckId = playerSource.getPlayerId(context);

                            return new StackTopCardsFromDeckEffect(context.getGame(), deckId, count, card);
                        } else
                            return new DoNothingEffect(context);
                    }
                });

        return result;
    }

}
