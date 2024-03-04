package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

import java.util.Objects;

public class PointBoxFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        String str = environment.getString(value.toString(), key);
        blueprint.setHasPointBox(!Objects.equals(str, "none"));

        str = str.replaceAll("[^\\d]", "");
        int pointsShown;
        if (str.isEmpty())
            pointsShown = 0;
        else
            pointsShown = Integer.parseInt(str);
        blueprint.setPointsShown(pointsShown);
    }
}
