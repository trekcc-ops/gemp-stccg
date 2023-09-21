package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.game.PlayConditions;
import org.json.simple.JSONObject;

public class PerPhaseLimit implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "limit", "perPlayer");

        final int limit = FieldUtils.getInteger(object.get("limit"), "limit", 1);
        final boolean perPlayer = FieldUtils.getBoolean(object.get("perPlayer"), "perPlayer", false);

        return (actionContext) -> {
            if (perPlayer)
                return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), actionContext.getPerformingPlayer() + "_", limit);
            else
                return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), limit);
        };
    }
}
