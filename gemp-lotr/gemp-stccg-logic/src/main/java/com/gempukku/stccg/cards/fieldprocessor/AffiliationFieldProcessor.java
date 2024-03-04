package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Affiliation;

public class AffiliationFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final String[] affiliations = environment.getString(value, key).split(",");
        for (String affiliation : affiliations) {
            blueprint.addAffiliation(environment.getEnum(Affiliation.class, affiliation));
        }
    }
}
