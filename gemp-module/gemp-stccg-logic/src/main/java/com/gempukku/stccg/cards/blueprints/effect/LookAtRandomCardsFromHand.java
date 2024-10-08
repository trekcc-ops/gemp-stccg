package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.revealcards.LookAtRandomCardsFromHandEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

public class LookAtRandomCardsFromHand implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "count", "memorize");

        final PlayerSource targetPlayer = PlayerResolver.resolvePlayer(effectObject.get("player").textValue());
        final ValueSource countSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String memorized = environment.getString(effectObject, "memorize", "_temp");

        return new DefaultDelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final String targetPlayerId = targetPlayer.getPlayerId(actionContext);
                return actionContext.getPerformingPlayer().canLookOrRevealCardsInHandOfPlayer(targetPlayerId) &&
                        actionContext.getGameState().getHand(targetPlayerId).size() >=
                                countSource.evaluateExpression(actionContext, null);
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String handPlayer = targetPlayer.getPlayerId(context);
                final int count = countSource.evaluateExpression(context, null);
                return new LookAtRandomCardsFromHandEffect(context, handPlayer, count, memorized);
            }
        };
    }

}