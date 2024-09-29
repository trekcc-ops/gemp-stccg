package com.gempukku.stccg.effectprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface EffectProcessor {

    default void processEffect(JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException { }

}
