package com.gempukku.stccg.cards.fieldprocessor;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.effectprocessor.TriggerEffectProcessor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TribblePowerFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        TribblePower tribblePower = environment.getEnum(TribblePower.class, value, key);
        blueprint.setTribblePower(tribblePower);

        if (tribblePower.isActive()) {
            JSONObject jsonObject;
            String jsonString = "{\"effect\":{\"type\":\"activateTribblePower\"}";
            if (tribblePower == TribblePower.AVALANCHE) {
                jsonString += ",\"requires\":{\"type\":\"cardsInHandMoreThan\",\"player\":\"you\",\"count\":3}";
            }
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