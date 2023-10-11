package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class ScorePoints implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "player");
        final ValueSource amount = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          ActionContext actionContext) {
                int points = amount.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                final String scoringPlayer = playerSource.getPlayer(actionContext);

                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        actionContext.getGame().getGameState().addToPlayerScore(scoringPlayer, points);
                    }
                };
            }
        };
    }
}
