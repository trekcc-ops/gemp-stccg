package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

public class Reconciles implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "player");

        final String player = FieldUtils.getString(value.get("player"), "player");

        PlayerSource playerSource = (player != null) ? PlayerResolver.resolvePlayer(player) : null;

        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                if (playerSource != null)
                    return TriggerConditions.reconciles(actionContext.getEffectResult(),
                            playerSource.getPlayerId(actionContext));
                else
                    return TriggerConditions.reconciles(actionContext.getEffectResult(), null);
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
