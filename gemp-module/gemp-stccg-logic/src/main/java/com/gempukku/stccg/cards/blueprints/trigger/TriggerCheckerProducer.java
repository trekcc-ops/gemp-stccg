package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface TriggerCheckerProducer {

    TriggerChecker getTriggerChecker(JsonNode value, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;
}