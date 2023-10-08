package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltLotroCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.Uniqueness;

public class UniquenessFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint,
                             CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        Uniqueness uniqueness = FieldUtils.getEnum(Uniqueness.class, value, key);
        blueprint.setUniqueness(uniqueness);
    }
}
