package com.gempukku.stccg.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.revealcards.LookAtRandomCardsFromHandEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class LookAtRandomCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "hand", "count", "memorize");

        final PlayerSource handSource = PlayerResolver.resolvePlayer(effectObject.get("hand").textValue());
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memorized = environment.getString(effectObject, "memorize", "_temp");

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final DefaultGame game = actionContext.getGame();
                final String handPlayer = handSource.getPlayerId(actionContext);
                final int count = countSource.evaluateExpression(actionContext, null);

                if (actionContext.getGameState().getHand(handPlayer).size() < count)
                    return false;

                return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, handPlayer, actionContext.getPerformingPlayerId());
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String handPlayer = handSource.getPlayerId(context);
                final int count = countSource.evaluateExpression(context, null);

                return new LookAtRandomCardsFromHandEffect(context, handPlayer, count) {
                    @Override
                    protected void cardsSeen(List<PhysicalCard> seenCards) {
                        context.setCardMemory(memorized, seenCards);
                    }
                };
            }
        };
    }

}
