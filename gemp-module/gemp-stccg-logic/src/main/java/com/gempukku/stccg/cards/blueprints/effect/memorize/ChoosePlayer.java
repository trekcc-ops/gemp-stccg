package com.gempukku.stccg.cards.blueprints.effect.memorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChoosePlayerEffect;
import com.gempukku.stccg.cards.blueprints.effect.DefaultDelayedAppender;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppender;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppenderProducer;

public class ChoosePlayer implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memorize");

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new ChoosePlayerEffect(context) {
                    @Override
                    protected void playerChosen(String playerId) {
                        context.setValueToMemory(effectObject.get("memorize").textValue(), playerId);
                    }
                };
            }
        };
    }

}