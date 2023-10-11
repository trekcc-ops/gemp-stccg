package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.effects.defaulteffect.LookAtRandomCardsFromHandEffect;
import com.gempukku.stccg.effects.Effect;
import org.json.simple.JSONObject;

import java.util.List;

public class LookAtRandomCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "hand", "count", "memorize");

        final String hand = FieldUtils.getString(effectObject.get("hand"), "hand");
        final PlayerSource handSource = PlayerResolver.resolvePlayer(hand);
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memorized = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final DefaultGame game = actionContext.getGame();
                final String handPlayer = handSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(game, null);

                if (actionContext.getGame().getGameState().getHand(handPlayer).size() < count)
                    return false;

                return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, handPlayer, actionContext.getPerformingPlayer());
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String handPlayer = handSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new LookAtRandomCardsFromHandEffect(actionContext, handPlayer, count) {
                    @Override
                    protected void cardsSeen(List<PhysicalCard> seenCards) {
                        actionContext.setCardMemory(memorized, seenCards);
                    }
                };
            }
        };
    }

}
