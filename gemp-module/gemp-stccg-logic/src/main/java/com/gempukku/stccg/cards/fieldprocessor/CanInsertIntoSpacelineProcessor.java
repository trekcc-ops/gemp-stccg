package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class CanInsertIntoSpacelineProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        blueprint.setCanInsertIntoSpaceline(environment.getBoolean(value, key));
    }
}