package com.gempukku.stccg.cards.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Affiliation;

public class ImageOptionsFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        if (value.isArray()) {
            for (JsonNode image : value) {
                Affiliation affiliation = environment.getEnum(Affiliation.class, image.get("affiliation").textValue(), "affiliation");
                blueprint.addImageOption(affiliation, image.get("image-url").textValue());
            }
        } else {
            throw new InvalidCardDefinitionException("Image options blueprint field could not be processed as an array");
        }
    }
}
