package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.CardBlueprint;

public class DirectionFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        blueprint.setDirection(FieldUtils.getEnum(CardBlueprint.Direction.class, value, key));
    }
}
