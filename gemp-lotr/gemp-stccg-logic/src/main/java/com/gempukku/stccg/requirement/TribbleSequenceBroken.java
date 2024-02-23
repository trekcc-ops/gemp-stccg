package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.TribblesActionContext;
import org.json.simple.JSONObject;

public class TribbleSequenceBroken extends RequirementProducer{
    @Override
    public Requirement getPlayRequirement(JSONObject object, CardGenerationEnvironment environment) {

        return actionContext -> {
            if (actionContext instanceof TribblesActionContext)
                return ((TribblesActionContext) actionContext).getGame().getGameState().isChainBroken();
            else return false;
        };
    }
}
