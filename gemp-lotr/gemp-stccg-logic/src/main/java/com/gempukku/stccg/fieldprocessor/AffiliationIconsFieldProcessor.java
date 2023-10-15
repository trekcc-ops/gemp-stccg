package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Affiliation;

import java.util.Objects;

public class AffiliationIconsFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String[] iconArray = FieldUtils.getString(value, key).split(",");
        for (String icon : iconArray) {
            if (Objects.equals(icon, "any")) {
                blueprint.setAnyCrewOrAwayTeamCanAttempt(true);
            } else {
                blueprint.addOwnerAffiliationIcon(FieldUtils.getEnum(Affiliation.class, icon));
            }
        }
    }
}
