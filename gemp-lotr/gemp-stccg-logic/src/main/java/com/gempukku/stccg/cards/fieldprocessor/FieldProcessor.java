package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface FieldProcessor {
    void processField(String key, Object value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException;
}
