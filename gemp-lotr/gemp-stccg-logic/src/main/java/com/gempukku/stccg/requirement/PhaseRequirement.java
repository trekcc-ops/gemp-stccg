package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Phase;
import org.json.simple.JSONObject;

public class PhaseRequirement extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "phase");

        final Phase phase = environment.getEnum(Phase.class, object.get("phase"), "phase");
        return (actionContext) -> actionContext.getGameState().getCurrentPhase() == phase;
    }
}
