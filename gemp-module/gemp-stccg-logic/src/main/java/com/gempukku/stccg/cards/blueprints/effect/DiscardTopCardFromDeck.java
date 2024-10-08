package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class DiscardTopCardFromDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "count", "forced", "memorize");

        final String memorize = effectObject.get("memorize").textValue();
        final ValueSource countSource =
                ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean forced = environment.getBoolean(effectObject, "forced");

        final PlayerSource playerSource =
                environment.getPlayerSource(effectObject, "player", true);

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