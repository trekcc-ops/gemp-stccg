package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Region;

public class RegionFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint, CardBlueprintFactory environment) throws InvalidCardDefinitionException {

        blueprint.setRegion(environment.getEnum(Region.class, value, key));
    }
}
