package com.gempukku.stccg.cards.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.CardIcon;

import java.util.LinkedList;
import java.util.List;

public class IconsFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final String[] icons = value.textValue().split(",");
        List<CardIcon> iconObjects = new LinkedList<>();
        for (String icon : icons) {
            iconObjects.add(environment.getEnum(CardIcon.class, icon));
        }
        blueprint.setIcons(iconObjects);
    }
}
