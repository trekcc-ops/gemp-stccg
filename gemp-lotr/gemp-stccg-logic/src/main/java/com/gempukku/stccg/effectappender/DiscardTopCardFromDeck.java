package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.Collection;

public class DiscardTopCardFromDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "deckowner", "count", "forced", "memorize");

        final String deckOwner = environment.getString(effectObject.get("deckowner"), "deckowner", "you");
        final String memorize = environment.getString(effectObject.get("memorize"), "memorize");
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean forced = environment.getBoolean(effectObject.get("forced"), "forced");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(deckOwner);

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String deckId = playerSource.getPlayerId(actionContext);
                final int count = countSource.evaluateExpression(actionContext, null);

                // Don't check if player can discard top cards, since it's a cost
                final DefaultGame game = actionContext.getGame();
                return game.getGameState().getDrawDeck(deckId).size() >= count
                        && (!forced || game.getModifiersQuerying().canDiscardCardsFromTopOfDeck(
                        actionContext.getPerformingPlayerId(), actionContext.getSource()));
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String deckId = playerSource.getPlayerId(context);
                final int count = countSource.evaluateExpression(context, null);

                return new DiscardCardsFromEndOfCardPileEffect(context.getGame(), context.getSource(), Zone.DRAW_DECK,
                        EndOfPile.TOP, deckId, count, forced) {
                    @Override
                    protected void cardsDiscardedCallback(Collection<PhysicalCard> cards) {
                        if (memorize != null)
                            context.setCardMemory(memorize, cards);
                    }
                };
            }
        };
    }

}
