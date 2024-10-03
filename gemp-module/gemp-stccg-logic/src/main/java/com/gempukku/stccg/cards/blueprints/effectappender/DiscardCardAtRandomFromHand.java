package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.discard.DiscardCardAtRandomFromHandEffect;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class DiscardCardAtRandomFromHand implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "player", "count", "forced");

        final PlayerSource playerSource = environment.getPlayerSource(effectObject, "player", true);
        final ValueSource countSource =
                ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final boolean forced = environment.getBoolean(effectObject, "forced");

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
