package com.gempukku.stccg.cards.blueprints.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface FieldProcessor {
    default void processField(String key, JsonNode value, CardBlueprint blueprint) throws InvalidCardDefinitionException { }
}