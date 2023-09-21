package com.gempukku.lotro.requirement.trigger;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public interface TriggerCheckerProducer {
    TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
