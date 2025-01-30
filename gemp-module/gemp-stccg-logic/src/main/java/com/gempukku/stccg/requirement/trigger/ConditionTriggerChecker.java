package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

public class ConditionTriggerChecker implements TriggerChecker {

    private final List<Requirement> _requirements;

    ConditionTriggerChecker(
            @JsonProperty(value = "requires", required = true)
            List<Requirement> requirements
    ) {
        _requirements = requirements;
    }
    @Override
    public boolean accepts(ActionContext actionContext) {
        return actionContext.acceptsAllRequirements(_requirements);
    }

}