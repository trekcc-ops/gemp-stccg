package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.IncrementTurnLimitEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class IncrementPerTurnLimit implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "limit");

        final int limit = FieldUtils.getInteger(effectObject.get("limit"), "limit", 1);

        return new DelayedAppender<>() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                return new IncrementTurnLimitEffect(actionContext.getSource(), limit);
            }

            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                return PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limit);
            }
        };
    }

}
