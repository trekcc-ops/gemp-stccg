package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Icon1E;

import java.util.LinkedList;
import java.util.List;

public class IconsFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final String[] icons = environment.getString(value, key).split(",");
        List<Icon1E> iconObjects = new LinkedList<>();
        for (String icon : icons) {
            iconObjects.add(environment.getEnum(Icon1E.class, icon));
        }
        blueprint.setIcons(iconObjects);
    }
}
