package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.ShuffleDeckEffect;
import com.gempukku.stccg.actions.revealcards.LookAtTopCardOfADeckEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import org.json.simple.JSONObject;

import java.util.List;

public class LookAtDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "deck", "memorize");

        final String deck = environment.getString(effectObject.get("deck"), "deck", "you");
        final String memorize = environment.getString(effectObject.get("memorize"), "memorize");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(new DefaultDelayedAppender() {

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String deckId = playerSource.getPlayerId(context);
                final int count = context.getGameState().getDrawDeck(deckId).size();

                return new LookAtTopCardOfADeckEffect(context, count, deckId) {
                    @Override
                    protected void cardsLookedAt(List<? extends PhysicalCard> cards) {
                        if (memorize != null)
                            context.setCardMemory(memorize, cards);
                    }
                };
            }
        });
        result.addEffectAppender(new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new ShuffleDeckEffect(context.getGame(), playerSource.getPlayerId(context));
            }
        });

        return result;
    }
}
