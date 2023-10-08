package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public interface RequirementProducer {
    <AbstractGame extends DefaultGame> Requirement<AbstractGame> getPlayRequirement(
            JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
