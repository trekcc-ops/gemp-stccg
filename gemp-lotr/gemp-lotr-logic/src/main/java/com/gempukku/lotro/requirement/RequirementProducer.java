package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

public interface RequirementProducer {
    <AbstractGame extends DefaultGame> Requirement<AbstractGame> getPlayRequirement(
            JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
