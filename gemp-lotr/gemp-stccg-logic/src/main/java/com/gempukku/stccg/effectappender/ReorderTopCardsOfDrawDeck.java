package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ReorderTopCardsOfDeckEffect;
import org.json.simple.JSONObject;

public class ReorderTopCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "player", "deck");
        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String player = environment.getString(effectObject.get("player"), "player", "you");
        final String deck = environment.getString(effectObject.get("deck"), "deck", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        final PlayerSource deckSource = PlayerResolver.resolvePlayer(deck);

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return actionContext.getGameState().getDrawDeck(deckSource.getPlayerId(actionContext)).size() >=
                        valueSource.evaluateExpression(actionContext, null);
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final int count = valueSource.evaluateExpression(context, null);
                return new ReorderTopCardsOfDeckEffect(context.getGame(), action, playerSource.getPlayerId(context),
                        deckSource.getPlayerId(context), count);
            }
        };
    }
}
