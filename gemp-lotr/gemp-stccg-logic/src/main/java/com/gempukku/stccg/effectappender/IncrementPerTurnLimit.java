package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.IncrementTurnLimitEffect;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class IncrementPerTurnLimit implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "limit");

        final int limit = environment.getInteger(effectObject.get("limit"), "limit", 1);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new IncrementTurnLimitEffect(context, limit);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limit);
            }
        };
    }

}
