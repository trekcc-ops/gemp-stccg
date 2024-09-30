package com.gempukku.stccg.cards.blueprints.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class StringFieldProcessor implements FieldProcessor {

    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        String valueString = value.textValue();
        switch(key) {
            case "image-url":
                blueprint.setImageUrl(valueString);
                break;
            case "lore":
                blueprint.setLore(valueString);
                break;
            case "rarity":
                blueprint.setRarity(valueString);
                break;
            case "subtitle":
                blueprint.setSubtitle(valueString);
                break;
            case "title":
                blueprint.setTitle(valueString);
                break;
            case "persona":
                blueprint.setPersona(valueString);
                break;
            default:
                throw new InvalidCardDefinitionException("Card definition error - Tried to use StringFieldProcessor on key " + key);
        }
    }
}
