package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Uniqueness;

public class UniquenessFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltCardBlueprint blueprint,
                             CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        Uniqueness uniqueness = FieldUtils.getEnum(Uniqueness.class, value, key);
        blueprint.setUniqueness(uniqueness);
    }
}
