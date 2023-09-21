package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PlayConditions;
import org.json.simple.JSONObject;

public class PhaseRequirement implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "phase");

        final Phase phase = FieldUtils.getEnum(Phase.class, object.get("phase"), "phase");
        return (actionContext) -> PlayConditions.isPhase(actionContext.getGame(), phase);
    }
}
