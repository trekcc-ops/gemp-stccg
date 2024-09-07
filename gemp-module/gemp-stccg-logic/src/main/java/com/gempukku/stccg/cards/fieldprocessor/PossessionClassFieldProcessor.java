package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.lotr.PossessionClass;

import java.util.HashSet;

public class PossessionClassFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final String[] stringArray = environment.getStringArray(value, key);

        HashSet<PossessionClass> result;
        if (stringArray.length == 0)
            result = null;
        else {
            result = new HashSet<>();
            for (String possessionClass : stringArray)
                result.add(environment.getEnum(PossessionClass.class, possessionClass, key));
        }
        blueprint.setPossessionClasses(result);
    }
}
