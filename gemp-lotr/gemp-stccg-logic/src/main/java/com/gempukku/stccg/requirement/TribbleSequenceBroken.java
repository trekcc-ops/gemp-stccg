package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import org.json.simple.JSONObject;

public class TribbleSequenceBroken extends RequirementProducer{
    @Override
    public TribblesRequirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) {

        return actionContext -> actionContext.getGame().getGameState().isChainBroken();
    }
}
