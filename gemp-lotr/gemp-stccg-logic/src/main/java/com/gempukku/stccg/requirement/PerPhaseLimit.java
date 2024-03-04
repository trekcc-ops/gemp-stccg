package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class PerPhaseLimit extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "limit", "perPlayer");

        final int limit = environment.getInteger(object.get("limit"), "limit", 1);
        final boolean perPlayer = environment.getBoolean(object.get("perPlayer"), "perPlayer", false);

        return (actionContext) -> {
            if (perPlayer)
                return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), actionContext.getPerformingPlayerId() + "_", limit);
            else
                return PlayConditions.checkPhaseLimit(actionContext.getGame(), actionContext.getSource(), limit);
        };
    }
}
