package com.gempukku.stccg.cards.blueprints.effect.memorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseTribblePowerEffect;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppender;
import com.gempukku.stccg.cards.blueprints.effect.EffectAppenderProducer;
import com.gempukku.stccg.cards.blueprints.effect.TribblesDelayedAppender;

public class ChooseTribblePower implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memorize");

        final String memorize = effectObject.get("memorize").textValue();

        return new TribblesDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                return new ChooseTribblePowerEffect(context) {
                    @Override
                    protected void powerChosen(String playerId) {
                        context.setValueToMemory(memorize, playerId);
                    }
                };
            }
        };
    }

}