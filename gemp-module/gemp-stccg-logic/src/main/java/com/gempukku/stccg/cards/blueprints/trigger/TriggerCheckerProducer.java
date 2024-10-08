package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface TriggerCheckerProducer {

    TriggerChecker getTriggerChecker(JsonNode value)
            throws InvalidCardDefinitionException;
}