package com.gempukku.stccg.cards.fieldprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.effectprocessor.TriggerEffectProcessor;

public class TribblePowerFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint,
                             CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        TribblePower tribblePower = environment.getEnum(TribblePower.class, value.textValue(), key);
        blueprint.setTribblePower(tribblePower);

        if (tribblePower.isActive()) {
            JsonNode node;
            String jsonString = "{\"effect\":{\"type\":\"activateTribblePower\"}";
            if (tribblePower == TribblePower.AVALANCHE) {
                jsonString += ",\"requires\":{\"type\":\"cardsInHandMoreThan\",\"player\":\"you\",\"count\":3}";
            }
            jsonString += ",\"optional\":true,\"trigger\":{\"filter\":\"self\",\"type\":\"played\"},\"type\":\"trigger\"}";

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                node = objectMapper.readTree(jsonString);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            new TriggerEffectProcessor().processEffect(node, blueprint, environment);

        }
    }
}