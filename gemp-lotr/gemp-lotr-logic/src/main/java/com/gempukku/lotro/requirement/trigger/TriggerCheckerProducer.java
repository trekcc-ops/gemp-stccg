package com.gempukku.lotro.requirement.trigger;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

public interface TriggerCheckerProducer<AbstractGame extends DefaultGame> {
    TriggerChecker<AbstractGame> getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
