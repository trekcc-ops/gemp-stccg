package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Affiliation;
import org.json.simple.JSONObject;

public class ImageOptionsFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        final JSONObject[] imageArray = environment.getObjectArray(value, key);
        for (JSONObject image : imageArray) {
            Affiliation affiliation = environment.getEnum(Affiliation.class, image.get("affiliation"), "affiliation");
            String imageUrl = environment.getString(image.get("image-url"), "image-url");
            blueprint.addImageOption(affiliation, imageUrl);
        }
    }
}
