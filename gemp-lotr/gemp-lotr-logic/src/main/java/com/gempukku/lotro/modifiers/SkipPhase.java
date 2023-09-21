package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.requirement.Requirement;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.common.Phase;
import org.json.simple.JSONObject;

public class SkipPhase implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "requires", "phase");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final Phase phase = FieldUtils.getEnum(Phase.class, object.get("phase"), "phase");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new ShouldSkipPhaseModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext), phase);
    }
}
