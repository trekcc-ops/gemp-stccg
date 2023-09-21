package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.lotronly.LotroGameUtils;
import org.json.simple.JSONObject;

public class IsSideRequirementProducer implements RequirementProducer{
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "side");

        final Side side = FieldUtils.getEnum(Side.class, object.get("side"), "side");

        return (Requirement<DefaultGame>) actionContext -> LotroGameUtils.isSide(actionContext.getGame(), side, actionContext.getPerformingPlayer());
    }
}
