package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ReorderTopCardsOfDeckEffect;

public class ReorderTopCardsOfDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "player", "deck");

        final ValueSource valueSource =
                ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);

        final PlayerSource playerSource = environment.getPlayerSource(effectObject, "player", true);
        final PlayerSource deckSource = environment.getPlayerSource(effectObject, "deck", true);

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
