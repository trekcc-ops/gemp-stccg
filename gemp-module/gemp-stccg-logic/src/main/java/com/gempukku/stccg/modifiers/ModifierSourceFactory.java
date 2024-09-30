package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;

import java.util.HashMap;
import java.util.Map;

public class ModifierSourceFactory {
    private final Map<String, ModifierSourceProducer> modifierProducers = new HashMap<>();

    public ModifierSourceFactory() {
        modifierProducers.put("gainicon", new AddIcon());
    }

    public ModifierSource getModifier(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        final String type = node.get("type").textValue();
        final ModifierSourceProducer modifierSourceProducer = modifierProducers.get(type.toLowerCase());
        if (modifierSourceProducer == null)
            throw new InvalidCardDefinitionException("Unable to resolve modifier of type: " + type);
        return modifierSourceProducer.getModifierSource(node, environment);
    }

}