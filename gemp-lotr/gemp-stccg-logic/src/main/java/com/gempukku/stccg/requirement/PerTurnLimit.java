package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class PerTurnLimit extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "limit");

        final int limit = environment.getInteger(object.get("limit"), "limit", 1);

        return (actionContext) ->
                PlayConditions.checkTurnLimit(actionContext.getGame(), actionContext.getSource(), limit);
    }
}