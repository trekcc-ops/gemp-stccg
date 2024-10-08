package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.IncrementTurnLimitEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;

public class IncrementPerTurnLimit implements EffectAppenderProducer {
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "limit");

        final int limit = BlueprintUtils.getInteger(effectObject, "limit", 1);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new IncrementTurnLimitEffect(context, limit);
            }

            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                return actionContext.getSource().checkTurnLimit(limit);
            }
        };
    }

}