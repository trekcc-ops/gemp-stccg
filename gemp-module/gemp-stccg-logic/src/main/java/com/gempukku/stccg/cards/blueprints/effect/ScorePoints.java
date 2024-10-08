package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

public class ScorePoints implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(effectObject, "amount", "player");

        final ValueSource amount = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        final PlayerSource playerSource = BlueprintUtils.getPlayerSource(effectObject, "player", true);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          ActionContext context) {
                int points = amount.evaluateExpression(context, null);
                final String scoringPlayer = playerSource.getPlayerId(context);

                return new UnrespondableEffect(context) {
                    @Override
                    protected void doPlayEffect() {
                        context.getGameState().addToPlayerScore(scoringPlayer, points);
                    }
                };
            }
        };
    }
}