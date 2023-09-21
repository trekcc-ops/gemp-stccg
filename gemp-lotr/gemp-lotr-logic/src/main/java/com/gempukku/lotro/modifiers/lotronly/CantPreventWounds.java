package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.requirement.Requirement;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.modifiers.ModifierSourceProducer;
import com.gempukku.lotro.modifiers.RequirementCondition;
import com.gempukku.lotro.modifiers.ModifierFlag;
import com.gempukku.lotro.modifiers.SpecialFlagModifier;
import org.json.simple.JSONObject;

public class CantPreventWounds implements ModifierSourceProducer {

    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object,"requires");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new SpecialFlagModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext), ModifierFlag.CANT_PREVENT_WOUNDS);
    }
}
