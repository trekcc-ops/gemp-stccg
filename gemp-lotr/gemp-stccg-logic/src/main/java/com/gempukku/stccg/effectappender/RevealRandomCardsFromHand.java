package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.RevealRandomCardsFromHandEffect;
import org.json.simple.JSONObject;

import java.util.List;

public class RevealRandomCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "hand", "forced", "count", "memorize");

        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final boolean forced = FieldUtils.getBoolean(effectObject.get("forced"), "forced");
        final String memorized = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final PlayerSource handSource = PlayerResolver.resolvePlayer(hand);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String handPlayer = handSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new RevealRandomCardsFromHandEffect(actionContext, handPlayer, count) {
                    @Override
                    protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                        actionContext.setCardMemory(memorized, revealedCards);
                    }
                };
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String handPlayer = handSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                if (actionContext.getGame().getGameState().getHand(handPlayer).size() < count)
                    return false;
                return !forced
                        || actionContext.getGame().getModifiersQuerying().canLookOrRevealCardsInHand(actionContext.getGame(), handPlayer, actionContext.getPerformingPlayer());
            }
        };
    }
}
