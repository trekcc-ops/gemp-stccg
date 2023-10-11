package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Quadrant;

public class QuadrantFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        blueprint.setQuadrant(FieldUtils.getEnum(Quadrant.class, value, key));
    }
}
