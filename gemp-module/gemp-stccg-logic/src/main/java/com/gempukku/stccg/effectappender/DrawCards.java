package com.gempukku.stccg.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.actions.draw.DrawCardsEffect;
import com.gempukku.stccg.actions.Effect;

public class DrawCards implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "player");

        final PlayerSource playerSource = environment.getPlayerSource(effectObject, "player", true);
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
