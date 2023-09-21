package com.gempukku.lotro.requirement;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequirementFactory {
    private final Map<String, RequirementProducer> requirementProducers = new HashMap<>();

    public RequirementFactory() {
        requirementProducers.put("not", new NotRequirementProducer());
        requirementProducers.put("or", new OrRequirementProducer());
        requirementProducers.put("canmove", new CanMove());
        requirementProducers.put("cardsindeckcount", new CardsInDeckCount());
        requirementProducers.put("cardsinhandmorethan", new CardsInHandMoreThan());
        requirementProducers.put("hascardindiscard", new HasCardInDiscard());
        requirementProducers.put("hascardinhand", new HasCardInHand());
        requirementProducers.put("hascardinplaypile", new HasCardInPlayPile());
        requirementProducers.put("hasinzonedata", new HasInZoneData());
        requirementProducers.put("isequal", new IsEqual());
        requirementProducers.put("isgreaterthan", new IsGreaterThan());
        requirementProducers.put("isgreaterthanorequal", new IsGreaterThanOrEqual());
        requirementProducers.put("islessthan", new IsLessThan());
        requirementProducers.put("islessthanorequal", new IsLessThanOrEqual());
        requirementProducers.put("isnotequal", new IsNotEqual());
        requirementProducers.put("isowner", new IsOwnerRequirementProducer());
        requirementProducers.put("isside", new IsSideRequirementProducer());
        requirementProducers.put("location", new Location());
        requirementProducers.put("memoryis", new MemoryIs());
        requirementProducers.put("memorylike", new MemoryLike());
        requirementProducers.put("memorymatches", new MemoryMatches());
        requirementProducers.put("movecountminimum", new MoveCountMinimum());
        requirementProducers.put("perphaselimit", new PerPhaseLimit());
        requirementProducers.put("perturnlimit", new PerTurnLimit());
        requirementProducers.put("phase", new PhaseRequirement());
        requirementProducers.put("playedcardthisphase", new PlayedCardThisPhase());
        requirementProducers.put("playerisnotself", new PlayerIsNotSelf());
    }

    public Requirement getRequirement(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String type = FieldUtils.getString(object.get("type"), "type");
        final RequirementProducer requirementProducer = requirementProducers.get(type.toLowerCase());
        if (requirementProducer == null)
            throw new InvalidCardDefinitionException("Unable to resolve requirement of type: " + type);
        return requirementProducer.getPlayRequirement(object, environment);
    }

    public Requirement[] getRequirements(JSONObject[] object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        Requirement[] result = new Requirement[object.length];
        for (int i = 0; i < object.length; i++)
            result[i] = getRequirement(object[i], environment);
        return result;
    }
}
