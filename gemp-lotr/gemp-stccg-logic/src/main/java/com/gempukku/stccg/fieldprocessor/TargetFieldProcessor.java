package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public class TargetFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String target = FieldUtils.getString(value, key);
        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(target, environment);
        blueprint.appendTargetFilter(filterableSource);
    }
}
