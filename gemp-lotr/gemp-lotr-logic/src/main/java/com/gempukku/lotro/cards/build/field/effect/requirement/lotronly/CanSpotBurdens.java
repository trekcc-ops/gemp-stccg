package com.gempukku.lotro.cards.build.field.effect.requirement.lotronly;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.requirement.RequirementProducer;
import com.gempukku.lotro.game.PlayConditions;
import org.json.simple.JSONObject;

public class CanSpotBurdens implements RequirementProducer {
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "amount");

        final int count = FieldUtils.getInteger(object.get("amount"), "amount", 1);
        return (actionContext) -> PlayConditions.canSpotBurdens(actionContext.getGame(), count);
    }
}
