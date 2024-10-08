package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class RequirementFactory {
    public static Requirement getRequirement(JsonNode object) throws InvalidCardDefinitionException {
        final String type = object.get("type").textValue().toLowerCase();
        return switch(type) {
            case "cardsindeckcount", "cardsinhandmorethan", "hascardindiscard", "hascardinhand", "hascardinplaypile",
                    "hasinzonedata", "nexttribbleinsequence" ->
                    new MiscRequirement(object);
            case "isequal", "isgreaterthan", "isgreaterthanorequal", "islessthan", "islessthanorequal", "isnotequal" ->
                    new ComparatorRequirementProducer().getPlayRequirement(object);
            case "isowner" -> new IsOwnerRequirementProducer().getPlayRequirement(object);
            case "lasttribbleplayed" -> new LastTribblePlayedProducer().getPlayRequirement(object);
            case "memoryis" -> new MemoryIs().getPlayRequirement(object);
            case "memorylike" -> new MemoryLike().getPlayRequirement(object);
            case "memorymatches" -> new MemoryMatches().getPlayRequirement(object);
            case "not" -> new NotRequirementProducer().getPlayRequirement(object);
            case "or" -> new OrRequirementProducer().getPlayRequirement(object);
            case "perturnlimit" -> new PerTurnLimit().getPlayRequirement(object);
            case "playedcardthisphase" -> new PlayedCardThisPhase().getPlayRequirement(object);
            case "tribblesequencebroken" -> new TribbleSequenceBroken().getPlayRequirement(object);
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