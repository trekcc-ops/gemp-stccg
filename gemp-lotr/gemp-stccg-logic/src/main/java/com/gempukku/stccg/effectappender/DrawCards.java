package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.actions.Effect;
import org.json.simple.JSONObject;

public class DrawCards implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "player");

        final String player = environment.getString(effectObject.get("player"), "player", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String drawPlayer = playerSource.getPlayerId(actionContext);
                final int cardCount = count.evaluateExpression(actionContext, null);
                return actionContext.getGameState().getDrawDeck(drawPlayer).size() >= cardCount;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String drawPlayer = playerSource.getPlayerId(context);
                final int cardsDrawn = count.evaluateExpression(context, null);
                return new DrawCardsEffect(context.getGame(), action, drawPlayer, cardsDrawn);
            }
        };
    }

}
