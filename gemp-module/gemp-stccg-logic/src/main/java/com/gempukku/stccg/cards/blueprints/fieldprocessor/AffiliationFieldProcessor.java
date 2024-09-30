package com.gempukku.stccg.cards.blueprints.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Affiliation;

public class AffiliationFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        final String[] affiliations = value.textValue().split(",");
        for (String affiliation : affiliations) {
            blueprint.addAffiliation(environment.getEnum(Affiliation.class, affiliation));
        }
    }
}
