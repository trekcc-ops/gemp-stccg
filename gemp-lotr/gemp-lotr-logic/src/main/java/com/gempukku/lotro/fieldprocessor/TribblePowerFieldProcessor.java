package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.common.TribblePower;
import org.apache.log4j.Logger;

public class TribblePowerFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final Logger LOG = Logger.getLogger(TribblePowerFieldProcessor.class);
        LOG.debug("Processing TribblePower field. " + value.toString());
        blueprint.setTribblePower(FieldUtils.getEnum(TribblePower.class, value, key));
    }
}
