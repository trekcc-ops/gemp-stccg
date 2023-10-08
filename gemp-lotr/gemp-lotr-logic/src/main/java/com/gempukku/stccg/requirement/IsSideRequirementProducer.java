package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.common.Side;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.lotronly.LotroGameUtils;
import org.json.simple.JSONObject;

public class IsSideRequirementProducer implements RequirementProducer{
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "side");

        final Side side = FieldUtils.getEnum(Side.class, object.get("side"), "side");

        return (Requirement<DefaultGame>) actionContext -> LotroGameUtils.isSide(actionContext.getGame(), side, actionContext.getPerformingPlayer());
    }
}
