package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.PlayConditions;
import org.json.simple.JSONObject;

public class PhaseRequirement extends RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "phase");

        final Phase phase = FieldUtils.getEnum(Phase.class, object.get("phase"), "phase");
        return (actionContext) -> actionContext.getGame().getGameState().getCurrentPhase() == phase;
    }
}
