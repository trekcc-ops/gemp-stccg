package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;

public class PlayerGoesOut implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value);
        PlayerSource playerSource = PlayerResolver.resolvePlayer("you");

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                return TriggerConditions.playerGoesOut(actionContext.getEffectResult(),
                        playerSource.getPlayerId(actionContext));
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
