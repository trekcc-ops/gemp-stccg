package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public abstract class RequirementProducer {

    public abstract Requirement getPlayRequirement(JsonNode node)
            throws InvalidCardDefinitionException;

}