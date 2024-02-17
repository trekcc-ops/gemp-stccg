package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class PlayerGoesOut implements TriggerCheckerProducer {
    @Override
    public TribblesTriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment)
            throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value);
        PlayerSource playerSource = PlayerResolver.resolvePlayer("you");

        return new TribblesTriggerChecker() {
            @Override
            public boolean accepts(TribblesActionContext actionContext) {
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
