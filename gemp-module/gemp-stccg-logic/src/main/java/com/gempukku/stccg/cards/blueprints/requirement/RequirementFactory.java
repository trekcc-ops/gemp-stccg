package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.common.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequirementFactory {
    public static Requirement getRequirement(JsonNode object) throws InvalidCardDefinitionException {
        final String type = object.get("type").textValue().toLowerCase();
        return switch(type) {
            case "cardsindeckcount", "cardsinhandmorethan", "hascardindiscard", "hascardinhand", "hascardinplaypile",
                    "lasttribbleplayed", "nexttribbleinsequence", "tribblesequencebroken" ->
                    new MiscRequirement(object);
            case "isequal", "isgreaterthan", "isgreaterthanorequal", "islessthan", "islessthanorequal", "isnotequal" ->
                    new ComparatorRequirement(object);
            case "isowner" -> actionContext ->
                    Objects.equals(actionContext.getPerformingPlayerId(), actionContext.getSource().getOwnerName());
            case "not" -> {
                Requirement requirement = getRequirement(object.get("requires"));
                yield actionContext -> !requirement.accepts(actionContext);
            }
            case "or" -> {
                Requirement[] requirements = getRequirements(object);
                yield actionContext -> actionContext.acceptsAnyRequirements(requirements);
            }
            case "perturnlimit" -> {
                int limit = BlueprintUtils.getInteger(object, "limit", 1);
                yield actionContext -> actionContext.getSource().checkTurnLimit(actionContext.getGame(), limit);
            }
            default -> throw new InvalidCardDefinitionException("Unable to resolve requirement of type: " + type);
        };
    }

    public static Requirement[] getRequirements(JsonNode parentNode) throws InvalidCardDefinitionException {
        List<Requirement> result = new ArrayList<>();
        if (parentNode.has("requires")) {
            List<JsonNode> requirements = JsonUtils.toArray(parentNode.get("requires"));
            for (JsonNode requirement : requirements)
                result.add(getRequirement(requirement));
        }
        return result.toArray(new Requirement[0]);
    }


}