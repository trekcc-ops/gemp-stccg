package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.common.PossessionClass;

import java.util.HashSet;
import java.util.Set;

public class PossessionClassFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String[] stringArray = FieldUtils.getStringArray(value, key);
        blueprint.setPossessionClasses(convertPossessionClasses(stringArray, key));
    }

    private Set<PossessionClass> convertPossessionClasses(String[] possessionClasses, String key) throws InvalidCardDefinitionException {
        if (possessionClasses.length == 0)
            return null;
        HashSet<PossessionClass> result = new HashSet<>();
        for (String possessionClass : possessionClasses)
            result.add(FieldUtils.getEnum(PossessionClass.class, possessionClass, key));

        return result;
    }
}
