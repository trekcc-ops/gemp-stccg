package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.revealcards.RevealBottomCardsOfDrawDeckEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

import java.util.List;

public class RevealBottomCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "deck", "count", "memorize");

        final String deck = environment.getString(effectObject.get("deck"), "deck", "you");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memorize = environment.getString(effectObject.get("memorize"), "memorize");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deck);

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String deckId = playerSource.getPlayerId(actionContext);
                final int count = valueSource.evaluateExpression(actionContext, null);

                return actionContext.getGameState().getDrawDeck(deckId).size() >= count;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          ActionContext context) {
                final String deckId = playerSource.getPlayerId(context);
                final int count = valueSource.evaluateExpression(context, null);

                return new RevealBottomCardsOfDrawDeckEffect(context, deckId, count) {
                    @Override
                    protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                        if (memorize != null)
                            context.setCardMemory(memorize, revealedCards);
                    }
                };
            }
        };
    }
}
