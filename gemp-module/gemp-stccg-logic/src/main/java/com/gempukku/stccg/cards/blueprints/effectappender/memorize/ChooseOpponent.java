package com.gempukku.stccg.cards.blueprints.effectappender.memorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseOpponentEffect;
import com.gempukku.stccg.cards.blueprints.effectappender.DefaultDelayedAppender;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppender;
import com.gempukku.stccg.cards.blueprints.effectappender.EffectAppenderProducer;

public class ChooseOpponent implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memorize");

        final String memorize = effectObject.get("memorize").textValue();

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new ChooseOpponentEffect(context) {
                    @Override
                    protected void playerChosen(String opponentId) {
                        context.setValueToMemory(memorize, opponentId);
                    }
                };
            }
        };
    }

}