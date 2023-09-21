package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;

public class TitleFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        boolean unique = false;
        String title = FieldUtils.getString(value, key);
        //Deprecated, don't do this.  Use the "unique" field instead.
        if (title.startsWith("*")) {
            unique = true;
            title = title.substring(1);
        }
        if (unique)
            blueprint.setUnique(true);
        blueprint.setTitle(title);
    }
}
