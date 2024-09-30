package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface ModifierSourceProducer {

    ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;

}
