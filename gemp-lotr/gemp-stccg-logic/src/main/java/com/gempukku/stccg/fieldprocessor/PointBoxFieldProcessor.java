package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class PointBoxFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltCardBlueprint blueprint,
                             CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        String str = FieldUtils.getString(value.toString(), key);
        if (str == "none")
            blueprint.setHasPointBox(false);
        else
            blueprint.setHasPointBox(true);

        str = str.replaceAll("[^\\d]", "");
        int pointsShown;
        if (str == "")
            pointsShown = 0;
        else
            pointsShown = Integer.parseInt(str);
        blueprint.setPointsShown(pointsShown);
    }
}
