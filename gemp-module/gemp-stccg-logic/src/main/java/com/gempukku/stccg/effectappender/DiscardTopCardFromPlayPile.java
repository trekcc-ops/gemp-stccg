package com.gempukku.stccg.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.discard.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;

import java.util.Collection;

public class DiscardTopCardFromPlayPile implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "deck", "count", "memorize");

        final String memorize = effectObject.get("memorize").textValue();
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final PlayerSource playerSource = environment.getPlayerSource(effectObject, "deck", true);

        return new TribblesDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final int count = countSource.evaluateExpression(actionContext, null);
                return actionContext.getGameState().getZoneCards(
                        playerSource.getPlayerId(actionContext), Zone.PLAY_PILE).size() >= count;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String deckId = playerSource.getPlayerId(context);
                final int count = countSource.evaluateExpression(context, null);

                return new DiscardCardsFromEndOfCardPileEffect(
                        context.getGame(), context.getSource(), Zone.PLAY_PILE, EndOfPile.TOP,
                        deckId, count, true) {
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


