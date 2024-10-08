package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.TribblesActionContext;

public class TribbleSequenceBroken extends RequirementProducer{
    @Override
    public Requirement getPlayRequirement(JsonNode node, CardBlueprintFactory environment) {

        return actionContext -> {
            if (actionContext instanceof TribblesActionContext)
                return ((TribblesActionContext) actionContext).getGame().getGameState().isChainBroken();
            else return false;
        };
    }
}