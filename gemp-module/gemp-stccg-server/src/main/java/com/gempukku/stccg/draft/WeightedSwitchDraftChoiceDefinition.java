package com.gempukku.stccg.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;

import java.io.IOException;
import java.util.*;

public class WeightedSwitchDraftChoiceDefinition implements DraftChoiceDefinition {
    public record WeightedChoice(float weight, DraftChoiceDefinition choice) { }

    private final Map<Float, DraftChoiceDefinition> _draftChoiceDefinitionMap;

    public WeightedSwitchDraftChoiceDefinition(
            @JsonProperty(value = "switchResult", required = true)
            List<WeightedChoice> choices
    ) throws IOException {
        if (choices != null && !choices.isEmpty()) {
            Map<Float, DraftChoiceDefinition> result = new HashMap<>();
            float weightTotal = 0;
            for (WeightedChoice weightedChoice : choices) {
                weightTotal += weightedChoice.weight;
                result.put(weightTotal, weightedChoice.choice);
            }
            _draftChoiceDefinitionMap = result;
        } else {
            throw new IOException("No weighted choices provided for weighted switch draft");
        }
    }

    @Override
    public Iterable<DraftChoice> getDraftChoice(long seed, int stage,
                                                DefaultCardCollection draftPool) {
        Random rnd = getRandom(seed, stage);
        float result = rnd.nextFloat();
        for (Map.Entry<Float, DraftChoiceDefinition> weightEntry : _draftChoiceDefinitionMap.entrySet()) {
            if (result < weightEntry.getKey())
                return weightEntry.getValue().getDraftChoice(seed, stage, draftPool);
        }

        return null;
    }

    @Override
    public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage)
            throws InvalidDraftResultException {
        Random rnd = getRandom(seed, stage);
        float result = rnd.nextFloat();
        for (Map.Entry<Float, DraftChoiceDefinition> weightEntry : _draftChoiceDefinitionMap.entrySet()) {
            if (result < weightEntry.getKey())
                return weightEntry.getValue().getCardsForChoiceId(choiceId, seed, stage);
        }
        throw new InvalidDraftResultException("Unable to get a result for draft selection");
    }

}