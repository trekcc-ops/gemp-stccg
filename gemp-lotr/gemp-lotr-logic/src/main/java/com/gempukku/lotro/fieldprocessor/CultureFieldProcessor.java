package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;

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
