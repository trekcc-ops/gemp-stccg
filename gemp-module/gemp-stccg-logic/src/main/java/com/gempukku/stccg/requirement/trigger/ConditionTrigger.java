package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

public class ConditionTrigger implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(value, "requires");

        List<Requirement> requirements = new ArrayList<>();
        for (JsonNode node : value) {
            try {
                requirements.add(new ObjectMapper().treeToValue(node, Requirement.class));
            } catch(JsonProcessingException exp) {
                throw new InvalidCardDefinitionException(exp.getMessage());
            }
        }

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                return actionContext.acceptsAllRequirements(requirements);
            }
        };
    }
}