package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.common.filterable.Quadrant;

public class CharacteristicFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        for (String characteristic : environment.getString(value, key).split(",")) {
                blueprint.addCharacteristic(environment.getEnum(Characteristic.class, characteristic));
        }
    }
}
