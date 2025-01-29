package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface TriggerCheckerProducer {

    TriggerChecker getTriggerChecker(JsonNode value)
            throws InvalidCardDefinitionException;
}