package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChoosePlayerExceptEffect;

public class ChoosePlayerExcept implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "memorize", "exclude");

        final String memorize = effectObject.get("memorize").textValue();
        final PlayerSource excludePlayerSource =
                environment.getPlayerSource(effectObject, "exclude", true);

        return new DefaultDelayedAppender() {
            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                final String excludedPlayer = excludePlayerSource.getPlayerId(context);
                return new ChoosePlayerExceptEffect(context, excludedPlayer) {
                    @Override
                    protected void playerChosen(String playerId) {
                        context.setValueToMemory(memorize, playerId);
                    }
                };
            }
        };
    }

}
