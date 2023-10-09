package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;
import com.gempukku.stccg.game.TribblesGame;

public class SetNextTribble implements EffectAppenderProducer {
    @Override
    public EffectAppender<TribblesGame> createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        final ValueSource amount = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action,
                                          DefaultActionContext<TribblesGame> actionContext) {
                int value = amount.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                return new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        actionContext.getGame().getGameState().setNextTribbleInSequence(value);
                    }
                };
            }
        };
    }
}
