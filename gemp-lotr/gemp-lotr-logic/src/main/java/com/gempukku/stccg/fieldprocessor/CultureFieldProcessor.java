package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltLotroCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Culture;
import com.gempukku.stccg.common.filterable.Side;

public class CultureFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final Culture culture = Culture.findCulture(FieldUtils.getString(value, key));
        blueprint.setCulture(culture);

        if (culture != Culture.GOLLUM) {
            assert culture != null;
            blueprint.setSide(culture.isFP() ? Side.FREE_PEOPLE : Side.SHADOW);
        }
    }
}
