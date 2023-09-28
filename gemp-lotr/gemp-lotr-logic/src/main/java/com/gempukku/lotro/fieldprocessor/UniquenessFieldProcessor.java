package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.common.Uniqueness;

public class UniquenessFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint,
                             CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        Uniqueness uniqueness = FieldUtils.getEnum(Uniqueness.class, value, key);
        blueprint.setUniqueness(uniqueness);
    }
}
