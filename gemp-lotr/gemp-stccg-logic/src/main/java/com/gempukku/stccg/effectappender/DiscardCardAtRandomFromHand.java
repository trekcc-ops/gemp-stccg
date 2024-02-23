package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.effects.defaulteffect.DiscardCardAtRandomFromHandEffect;
import com.gempukku.stccg.effects.Effect;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class DiscardCardAtRandomFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "hand", "count", "forced");

        final String player = FieldUtils.getString(effectObject.get("hand"), "hand", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean forced = FieldUtils.getBoolean(effectObject.get("forced"), "forced");

        return new DefaultDelayedAppender() {
            @Override
            protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String playerId = playerSource.getPlayerId(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                List<Effect> result = new LinkedList<>();
                for (int i = 0; i < count; i++)
                    result.add(new DiscardCardAtRandomFromHandEffect(actionContext, playerId, forced));

                return result;
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final DefaultGame game = actionContext.getGame();
                final String playerId = playerSource.getPlayerId(actionContext);
                final int count = countSource.getEvaluator(actionContext).evaluateExpression(game, null);
                return game.getGameState().getHand(playerId).size() >= count
                        && (!forced || game.getModifiersQuerying().canDiscardCardsFromHand(playerId, actionContext.getSource()));
            }
        };
    }

}
