package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltLotroCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface FieldProcessor {
    void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
