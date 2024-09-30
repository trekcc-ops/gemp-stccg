package com.gempukku.stccg.cards.blueprints.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class LocationFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        blueprint.setLocation(value.textValue());
    }
}
