package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.unrespondable.IncrementTurnLimitEffect;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class IncrementPerTurnLimit implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "limit");

        final int limit = FieldUtils.getInteger(effectObject.get("limit"), "limit", 1);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new IncrementTurnLimitEffect(actionContext, limit);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limit);
            }
        };
    }

}
