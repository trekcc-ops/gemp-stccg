package com.gempukku.stccg.cards.blueprints.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

import java.util.Objects;

public class PointBoxFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        String str;
        if (value.isTextual())
            str = value.textValue();
        else str = value.toString();
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
