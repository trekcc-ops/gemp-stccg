package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

import java.util.HashMap;
import java.util.Map;

public class RequirementFactory {
    private final Map<String, RequirementProducer> requirementProducers = new HashMap<>();

    @SuppressWarnings("SpellCheckingInspection")
    public RequirementFactory() {

        requirementProducers.put("not", new NotRequirementProducer());
        requirementProducers.put("or", new OrRequirementProducer());
        requirementProducers.put("cardsindeckcount", new CardsInDeckCount());
        requirementProducers.put("cardsinhandmorethan", new CardsInHandMoreThan());
        requirementProducers.put("hascardindiscard", new HasCardInDiscard());
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
        requirementProducers.put("hascardinplaypile", new HasCardInPlayPile());
        requirementProducers.put("lasttribbleplayed", new LastTribblePlayedProducer());
        requirementProducers.put("nexttribbleinsequence", new NextTribbleInSequenceRequirement());
        requirementProducers.put("tribblesequencebroken", new TribbleSequenceBroken());
    }

    public Requirement getRequirement(JsonNode object) throws InvalidCardDefinitionException {
        final String type = object.get("type").textValue().toLowerCase();
        final RequirementProducer requirementProducer = requirementProducers.get(type);
        if (requirementProducer == null)
            throw new InvalidCardDefinitionException("Unable to resolve requirement of type: " + type);
        return requirementProducer.getPlayRequirement(object);
    }

}