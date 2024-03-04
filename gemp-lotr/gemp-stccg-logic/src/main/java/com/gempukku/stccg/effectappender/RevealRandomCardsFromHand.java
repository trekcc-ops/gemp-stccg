package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.revealcards.RevealRandomCardsFromHandEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

import java.util.List;

public class RevealRandomCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "hand", "forced", "count", "memorize");

        final String hand = environment.getString(effectObject.get("hand"), "hand", "you");
        final boolean forced = environment.getBoolean(effectObject.get("forced"), "forced");
        final String memorized = environment.getString(effectObject.get("memorize"), "memorize", "_temp");

        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final PlayerSource handSource = PlayerResolver.resolvePlayer(hand);

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
