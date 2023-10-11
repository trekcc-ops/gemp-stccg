package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import org.json.simple.JSONObject;

public interface TriggerCheckerProducer {
    TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
