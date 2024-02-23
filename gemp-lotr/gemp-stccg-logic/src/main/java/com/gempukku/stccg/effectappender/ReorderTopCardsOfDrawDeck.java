package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ReorderTopCardsOfDeckEffect;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class ReorderTopCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "count", "player", "deck");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final String deck = FieldUtils.getString(effectObject.get("deck"), "deck", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final PlayerSource deckSource = PlayerResolver.resolvePlayer(deck);

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final Evaluator count = valueSource.getEvaluator(actionContext);
                return actionContext.getGameState().getDrawDeck(deckSource.getPlayerId(actionContext)).size() >= count.evaluateExpression(actionContext.getGame(), null);
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                final int count = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                return new ReorderTopCardsOfDeckEffect(actionContext.getGame(), action, playerSource.getPlayerId(actionContext), deckSource.getPlayerId(actionContext), count);
            }
        };
    }
}
