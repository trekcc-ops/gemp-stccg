package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.common.Quadrant;

public class QuadrantFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        blueprint.setQuadrant(FieldUtils.getEnum(Quadrant.class, value, key));
    }
}
