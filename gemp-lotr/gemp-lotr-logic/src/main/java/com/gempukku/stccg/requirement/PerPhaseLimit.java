package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.PlayConditions;
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
