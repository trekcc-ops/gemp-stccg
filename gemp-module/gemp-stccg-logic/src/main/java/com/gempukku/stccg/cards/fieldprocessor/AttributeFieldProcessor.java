package com.gempukku.stccg.cards.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.CardAttribute;

public class AttributeFieldProcessor implements FieldProcessor {
    private final CardAttribute _attribute;
    public AttributeFieldProcessor(CardAttribute attribute) {
        _attribute = attribute;
    }
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) {
        blueprint.setAttribute(_attribute, value.asInt());
    }
}
