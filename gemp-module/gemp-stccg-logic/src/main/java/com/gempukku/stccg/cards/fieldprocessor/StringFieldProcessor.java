package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class StringFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        switch(key) {
            case "image-url":
                blueprint.setImageUrl(environment.getString(value, key));
                break;
            case "rarity":
                blueprint.setRarity(environment.getString(value, key));
                break;
            default:
                throw new InvalidCardDefinitionException("Card definition error - Tried to use StringFieldProcessor on key " + key);
        }
    }
}
