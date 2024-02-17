package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.stccg.evaluator.Evaluator;
import org.json.simple.JSONObject;

public class PlaceTopCardOfDrawDeckOnTopOfPlayPile implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player", "count");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final ValueSource count = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String drawPlayer = playerSource.getPlayerId(actionContext);
                final Evaluator evaluator = count.getEvaluator(null);
                final int cardCount = evaluator.evaluateExpression(actionContext.getGame(), null);
                return actionContext.getGameState().getDrawDeck(drawPlayer).size() >= cardCount;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final String drawPlayer = playerSource.getPlayerId(actionContext);
                final Evaluator evaluator = count.getEvaluator(actionContext);
                final int cardsDrawn = evaluator.evaluateExpression(actionContext.getGame(), null);
                return new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(actionContext, drawPlayer, cardsDrawn);
            }
        };
    }

}