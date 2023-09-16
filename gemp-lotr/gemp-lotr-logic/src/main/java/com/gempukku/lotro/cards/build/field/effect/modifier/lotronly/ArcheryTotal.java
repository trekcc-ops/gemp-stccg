package com.gempukku.lotro.cards.build.field.effect.modifier.lotronly;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.ModifierSource;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.modifier.ModifierSourceProducer;
import com.gempukku.lotro.cards.build.field.effect.modifier.RequirementCondition;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.modifiers.lotronly.ArcheryTotalModifier;
import org.json.simple.JSONObject;

public class ArcheryTotal implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "amount", "requires", "side");

        final JSONObject[] conditionArray = FieldUtils.getObjectArray(object.get("requires"), "requires");
        final int amount = FieldUtils.getInteger(object.get("amount"), "amount");
        final Side side = FieldUtils.getEnum(Side.class, object.get("side"), "side");

        final Requirement[] requirements = environment.getRequirementFactory().getRequirements(conditionArray, environment);

        return actionContext -> new ArcheryTotalModifier(actionContext.getSource(), side,
                new RequirementCondition(requirements, actionContext),
                amount);
    }
}
