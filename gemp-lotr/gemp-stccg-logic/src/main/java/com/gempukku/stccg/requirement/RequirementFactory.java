package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.producers.*;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequirementFactory {
    private final Map<String, RequirementProducer> requirementProducers = new HashMap<>();
    private final CardBlueprintFactory _environment;

    @SuppressWarnings("SpellCheckingInspection")
    public RequirementFactory(CardBlueprintFactory environment) {
        _environment = environment;

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

    public Requirement getRequirement(JSONObject object) throws InvalidCardDefinitionException {
        final String type = _environment.getString(object.get("type"), "type");
        final RequirementProducer requirementProducer = requirementProducers.get(type.toLowerCase());
        if (requirementProducer == null)
            throw new InvalidCardDefinitionException("Unable to resolve requirement of type: " + type);
        return requirementProducer.getPlayRequirement(object, _environment);
    }

    public Requirement[] getRequirements(JSONObject[] object) throws InvalidCardDefinitionException {
        Requirement[] result = new Requirement[object.length];
        for (int i = 0; i < object.length; i++)
            result[i] = getRequirement(object[i]);
        return result;
    }
}
