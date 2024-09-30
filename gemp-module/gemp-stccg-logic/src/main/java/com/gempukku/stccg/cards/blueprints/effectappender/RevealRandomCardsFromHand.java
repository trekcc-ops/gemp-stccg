package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.revealcards.RevealRandomCardsFromHandEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

import java.util.List;

public class RevealRandomCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "hand", "forced", "count", "memorize");

        final boolean forced = environment.getBoolean(effectObject, "forced");
        final String memorized = environment.getString(effectObject, "memorize", "_temp");

        final ValueSource countSource =
                ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final PlayerSource handSource = environment.getPlayerSource(effectObject, "hand", true);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String handPlayer = handSource.getPlayerId(context);
                final int count = countSource.evaluateExpression(context, null);

                return new RevealRandomCardsFromHandEffect(context, handPlayer, count) {
                    @Override
                    protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                        context.setCardMemory(memorized, revealedCards);
                    }
                };
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String handPlayer = handSource.getPlayerId(actionContext);
                final int count = countSource.evaluateExpression(actionContext, null);
                if (actionContext.getGameState().getHand(handPlayer).size() < count)
                    return false;
                return !forced
                        || actionContext.getGame().getModifiersQuerying().canLookOrRevealCardsInHand(actionContext.getGame(), handPlayer, actionContext.getPerformingPlayerId());
            }
        };
    }
}
