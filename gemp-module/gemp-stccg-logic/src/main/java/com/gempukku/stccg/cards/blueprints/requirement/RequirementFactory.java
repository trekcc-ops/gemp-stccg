package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class RequirementFactory {
    public static Requirement getRequirement(JsonNode object) throws InvalidCardDefinitionException {
        final String type = object.get("type").textValue().toLowerCase();
        final RequirementProducer requirementProducer = switch(type) {
            case "cardsindeckcount", "cardsinhandmorethan", "hascardindiscard", "hascardinhand", "hascardinplaypile",
                    "hasinzonedata", "nexttribbleinsequence" ->
                    new MiscRequirementProducer();
            case "isequal", "isgreaterthan", "isgreaterthanorequal", "islessthan", "islessthanorequal", "isnotequal" ->
                    new ComparatorRequirementProducer();
            case "isowner" -> new IsOwnerRequirementProducer();
            case "lasttribbleplayed" -> new LastTribblePlayedProducer();
            case "memoryis" -> new MemoryIs();
            case "memorylike" -> new MemoryLike();
            case "memorymatches" -> new MemoryMatches();
            case "not" -> new NotRequirementProducer();
            case "or" -> new OrRequirementProducer();
            case "perphaselimit" -> new PerPhaseLimit();
            case "perturnlimit" -> new PerTurnLimit();
            case "phase" -> new PhaseRequirement();
            case "playedcardthisphase" -> new PlayedCardThisPhase();
            case "playerisnotself" -> new PlayerIsNotSelf();
            case "tribblesequencebroken" -> new TribbleSequenceBroken();
            default -> throw new InvalidCardDefinitionException("Unable to resolve requirement of type: " + type);
        };
        return requirementProducer.getPlayRequirement(object);
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