package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.JsonUtils;

import java.util.LinkedList;
import java.util.List;

public class RequirementFactory {

    /*     requirementProducers.put("hascardindiscard", new HasCardInDiscard());
    requirementProducers.put("hascardinhand", new HasCardInHand());
    requirementProducers.put("hasinzonedata", new HasInZoneData());
    requirementProducers.put("isequal", new IsEqual());
    requirementProducers.put("isgreaterthan", new IsGreaterThan());
    requirementProducers.put("isgreaterthanorequal", new IsGreaterThanOrEqual());
    requirementProducers.put("islessthan", new IsLessThan());
    requirementProducers.put("islessthanorequal", new IsLessThanOrEqual());
    requirementProducers.put("isnotequal", new IsNotEqual());
    requirementProducers.put("isowner", new IsOwnerRequirementProducer());
    requirementProducers.put("memoryis", new MemoryIs());
    requirementProducers.put("memorylike", new MemoryLike());
    requirementProducers.put("memorymatches", new MemoryMatches());
    requirementProducers.put("perphaselimit", new PerPhaseLimit());
    requirementProducers.put("perturnlimit", new PerTurnLimit());
    requirementProducers.put("phase", new PhaseRequirement());
    requirementProducers.put("playedcardthisphase", new PlayedCardThisPhase());
    requirementProducers.put("playerisnotself", new PlayerIsNotSelf());
    // Tribbles specific
 */

    public static Requirement getRequirement(JsonNode object) throws InvalidCardDefinitionException {
        final String type = object.get("type").textValue().toLowerCase();
        return switch (type) {
            case "cardsindeckcount" -> new CardsInDeckCount().getPlayRequirement(object);
            case "cardsinhandmorethan" -> new CardsInHandMoreThan().getPlayRequirement(object);
            case "hascardinplaypile" -> new HasCardInPlayPile().getPlayRequirement(object);
            case "lasttribbleplayed" -> new LastTribblePlayedProducer().getPlayRequirement(object);
            case "nexttribbleinsequence" -> new NextTribbleInSequenceRequirement().getPlayRequirement(object);
            case "not" -> new NotRequirementProducer().getPlayRequirement(object);
            case "or" -> new OrRequirementProducer().getPlayRequirement(object);
            case "tribblesequencebroken" -> new TribbleSequenceBroken().getPlayRequirement(object);
            default -> throw new InvalidCardDefinitionException("Unable to resolve requirement of type: " + type);
        };
    }

    public static Requirement[] getRequirementsFromJSON(JsonNode parentNode) throws InvalidCardDefinitionException {
        List<Requirement> result = new LinkedList<>();
        if (parentNode.has("requires")) {
            List<JsonNode> requirements = JsonUtils.toArray(parentNode.get("requires"));
            for (JsonNode requirement : requirements)
                result.add(getRequirement(requirement));
        }
        return result.toArray(new Requirement[0]);
    }
}