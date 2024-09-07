package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Affiliation;

public class AffiliationIconsFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        for (String icon : environment.getString(value, key).split(",")) {
            if (icon.equals("any")) {
                blueprint.setAnyCrewOrAwayTeamCanAttempt(true);
            } else {
                blueprint.addOwnerAffiliationIcon(environment.getEnum(Affiliation.class, icon));
            }
        }
    }
}
