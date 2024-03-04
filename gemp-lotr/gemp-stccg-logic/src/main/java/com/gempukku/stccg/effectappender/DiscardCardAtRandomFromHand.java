package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.discard.DiscardCardAtRandomFromHandEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class DiscardCardAtRandomFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "hand", "count", "forced");

        final String player = environment.getString(effectObject.get("hand"), "hand", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean forced = environment.getBoolean(effectObject.get("forced"), "forced");

        return new DefaultDelayedAppender() {
            @Override
            protected List<Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String playerId = playerSource.getPlayerId(actionContext);
                final int count = countSource.evaluateExpression(actionContext, null);

                List<Effect> result = new LinkedList<>();
                for (int i = 0; i < count; i++)
                    result.add(new DiscardCardAtRandomFromHandEffect(actionContext, playerId, forced));

                return result;
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final DefaultGame game = actionContext.getGame();
                final String playerId = playerSource.getPlayerId(actionContext);
                final int count = countSource.evaluateExpression(actionContext, null);
                return game.getGameState().getHand(playerId).size() >= count
                        && (!forced || game.getModifiersQuerying().canDiscardCardsFromHand(playerId, actionContext.getSource()));
            }
        };
    }

}
