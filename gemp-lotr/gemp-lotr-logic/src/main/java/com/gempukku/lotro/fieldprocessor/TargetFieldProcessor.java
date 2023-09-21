package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;

public class TargetFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String target = FieldUtils.getString(value, key);
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(target, environment);
        blueprint.appendTargetFilter(filterableSource);
    }
}
