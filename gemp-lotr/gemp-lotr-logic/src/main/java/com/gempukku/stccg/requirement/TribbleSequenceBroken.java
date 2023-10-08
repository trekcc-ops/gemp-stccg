package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.game.TribblesGame;
import org.json.simple.JSONObject;

public class TribbleSequenceBroken implements RequirementProducer{
    @Override
    public Requirement<TribblesGame> getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) {

        return actionContext -> actionContext.getGame().getGameState().isChainBroken();
    }
}
