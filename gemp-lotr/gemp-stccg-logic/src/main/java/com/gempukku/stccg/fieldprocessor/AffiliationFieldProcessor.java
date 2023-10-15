package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Affiliation;

public class AffiliationFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String[] affiliations = FieldUtils.getString(value, key).split(",");
        for (String affiliation : affiliations) {
            blueprint.addAffiliation(FieldUtils.getEnum(Affiliation.class, affiliation));
        }
    }
}
