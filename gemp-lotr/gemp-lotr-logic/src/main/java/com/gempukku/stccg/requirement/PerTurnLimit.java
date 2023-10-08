package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class PerTurnLimit implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "limit");

        final int limit = FieldUtils.getInteger(object.get("limit"), "limit", 1);

        return (actionContext) -> PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limit);
    }
}
