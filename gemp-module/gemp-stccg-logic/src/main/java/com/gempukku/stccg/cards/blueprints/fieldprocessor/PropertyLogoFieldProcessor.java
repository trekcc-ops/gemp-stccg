package com.gempukku.stccg.cards.blueprints.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.PropertyLogo;

public class PropertyLogoFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        blueprint.setPropertyLogo(environment.getEnum(PropertyLogo.class, node.textValue(), key));
    }
}
