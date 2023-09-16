package com.gempukku.lotro.cards.build.field.effect.requirement;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.cards.build.field.FieldUtils;
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
