package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.effects.LookAtRandomCardsFromHandEffect;
import com.gempukku.lotro.effects.Effect;
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

        return new DelayedAppender<>() {
            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                final DefaultGame game = actionContext.getGame();
                final String handPlayer = handSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(game, null);

                if (actionContext.getGame().getGameState().getHand(handPlayer).size() < count)
                    return false;

                return game.getModifiersQuerying().canLookOrRevealCardsInHand(game, handPlayer, actionContext.getPerformingPlayer());
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final String handPlayer = handSource.getPlayer(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new LookAtRandomCardsFromHandEffect(actionContext.getPerformingPlayer(), handPlayer, actionContext.getSource(), count) {
                    @Override
                    protected void cardsSeen(List<LotroPhysicalCard> seenCards) {
                        actionContext.setCardMemory(memorized, seenCards);
                    }
                };
            }
        };
    }

}
