package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.UnrespondableEffect;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

public class ScorePoints implements EffectAppenderProducer {
    @Override
    public EffectAppender<DefaultGame> createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "player");
        final ValueSource amount = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");
        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          DefaultActionContext<DefaultGame> actionContext) {
                int points = amount.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                final String scoringPlayer = playerSource.getPlayer(actionContext);

                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        actionContext.getGame().getGameState().addToPlayerScore(scoringPlayer, points);
                    }
                };
            }
        };
    }
}
