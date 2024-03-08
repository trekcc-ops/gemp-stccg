package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.CardIcon;

import java.util.LinkedList;
import java.util.List;

public class StaffingFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final String[] staffing = environment.getString(value, key).split(",");
        List<CardIcon> staffingIcons = new LinkedList<>();
        for (String icon : staffing) {
            staffingIcons.add(environment.getEnum(CardIcon.class, icon));
        }
        blueprint.setStaffing(staffingIcons);
    }
}
