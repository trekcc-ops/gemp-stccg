package com.gempukku.lotro.fieldprocessor;

import com.gempukku.lotro.cards.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.common.TribblePower;
import com.gempukku.lotro.effectprocessor.TriggerEffectProcessor;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TribblePowerFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final Logger LOG = Logger.getLogger(TribblePowerFieldProcessor.class);
        LOG.debug("Processing TribblePower field. " + value.toString());
        TribblePower tribblePower = FieldUtils.getEnum(TribblePower.class, value, key);
        blueprint.setTribblePower(tribblePower);
        String jsonString = "";
        JSONObject jsonObject;

        if (tribblePower == TribblePower.DISCARD) {
/*            jsonString += "{\"type\":\"trigger\",\"optional\":\"true\",";
            jsonString += "\"trigger\":{\"filter\":\"self\",\"type\":\"played\"},";
            jsonString += "\"effect\":{\"forced\":true,\"type\":\"discardFromHand\"}}";*/
                // The code below has been tested and works
//            jsonString += "{\"effect\":{\"forced\":true,\"type\":\"discardFromHand\"}";
//            jsonString += ",\"optional\":true,\"trigger\":{\"filter\":\"self\",\"type\":\"played\"},\"type\":\"trigger\"}";
            jsonString += "{\"effect\":{\"type\":\"activateTribblePower\"}";
            jsonString += ",\"optional\":true,\"trigger\":{\"filter\":\"self\",\"type\":\"played\"},\"type\":\"trigger\"}";

            try {
                JSONParser parser = new JSONParser();
                jsonObject = (JSONObject) parser.parse(jsonString);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            new TriggerEffectProcessor().processEffect(jsonObject, blueprint, environment);

        }
    }
}
