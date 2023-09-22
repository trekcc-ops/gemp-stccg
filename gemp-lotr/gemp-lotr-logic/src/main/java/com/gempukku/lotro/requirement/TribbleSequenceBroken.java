package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.game.TribblesGame;
import org.json.simple.JSONObject;

public class TribbleSequenceBroken implements RequirementProducer{
    @Override
    public Requirement<TribblesGame> getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) {

        return actionContext -> actionContext.getGame().getGameState().isChainBroken();
    }
}
