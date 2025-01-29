package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;

public class PlayerGoesOut implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(value);
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