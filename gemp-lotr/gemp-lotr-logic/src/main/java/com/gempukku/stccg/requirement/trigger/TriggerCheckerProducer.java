package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public interface TriggerCheckerProducer<AbstractGame extends DefaultGame> {
    TriggerChecker<AbstractGame> getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
