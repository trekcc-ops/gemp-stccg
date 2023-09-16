package com.gempukku.lotro.cards.build.field.effect.modifier;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.modifiers.PlayersCantPlayPhaseEventsOrPhaseSpecialAbilitiesModifier;
import org.json.simple.JSONObject;

public class CantPlayPhaseEventsOrPhaseSpecialAbilities implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "phase", "requires");

        final Phase phase = FieldUtils.getEnum(Phase.class, object.get("phase"), "phase");
        if (phase == null)
            throw new InvalidCardDefinitionException("Has to have a phase defined");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new PlayersCantPlayPhaseEventsOrPhaseSpecialAbilitiesModifier(actionContext.getSource(),
                new RequirementCondition(requirements, actionContext), phase);
    }
}
