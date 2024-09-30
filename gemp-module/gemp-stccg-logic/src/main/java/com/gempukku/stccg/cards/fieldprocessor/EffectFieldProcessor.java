package com.gempukku.stccg.cards.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectprocessor.*;

import java.util.HashMap;
import java.util.Map;

public class EffectFieldProcessor implements FieldProcessor {
    private final Map<String, EffectProcessor> effectProcessors = new HashMap<>();

    public EffectFieldProcessor() {
        effectProcessors.put("action", new ActionSourceAppender()); // appends ActionSource
        effectProcessors.put("modifier", new ModifierSourceAppender()); // appends ModifierSource
        effectProcessors.put("playoutofsequence", new PlayOutOfSequenceConditionAppender()); // appends Requirement
        effectProcessors.put("seed", new SeedCardActionSourceAppender());
    }

    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        if (value.isArray()) {
            for (JsonNode effect : value) {
                processJsonEffect(effect, blueprint, environment);
            }
        } else {
            processJsonEffect(value, blueprint, environment);
        }
    }

    private void processJsonEffect(JsonNode value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        final String effectType = value.get("type").textValue().toLowerCase();
        final EffectProcessor effectProcessor = effectProcessors.get(effectType);
        if (effectProcessor == null)
            throw new InvalidCardDefinitionException("Unable to find effect of type: " + effectType);
        effectProcessor.processEffect(value, blueprint, environment);
    }
}