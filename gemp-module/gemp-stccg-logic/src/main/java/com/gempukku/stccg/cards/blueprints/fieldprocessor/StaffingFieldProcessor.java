package com.gempukku.stccg.cards.blueprints.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.CardIcon;

import java.util.LinkedList;
import java.util.List;

public class StaffingFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        List<CardIcon> staffingIcons = new LinkedList<>();
        for (String icon : value.textValue().split(",")) {
            staffingIcons.add(environment.getEnum(CardIcon.class, icon));
        }
        blueprint.setStaffing(staffingIcons);
    }
}
