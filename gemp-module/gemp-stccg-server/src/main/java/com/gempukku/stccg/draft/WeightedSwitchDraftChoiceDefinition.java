package com.gempukku.stccg.draft;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.common.JsonUtils;

import java.util.*;

public class WeightedSwitchDraftChoiceDefinition implements DraftChoiceDefinition {

    private final Map<Float, DraftChoiceDefinition> _draftChoiceDefinitionMap = new LinkedHashMap<>();

    public WeightedSwitchDraftChoiceDefinition(JsonNode node, SoloDraftDefinitions builder) {
        List<JsonNode> switchResult = JsonUtils.toArray(node.get("switchResult"));

        float weightTotal = 0;
        for (JsonNode switchResultObject : switchResult) {
            float weight = switchResultObject.get("weight").floatValue();
            weightTotal += weight;
            _draftChoiceDefinitionMap.put(weightTotal, builder.buildDraftChoiceDefinition(switchResultObject));
        }
    }

    @Override
    public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
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
    public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
        Random rnd = getRandom(seed, stage);
        float result = rnd.nextFloat();
        for (Map.Entry<Float, DraftChoiceDefinition> weightEntry : _draftChoiceDefinitionMap.entrySet()) {
            if (result < weightEntry.getKey())
                return weightEntry.getValue().getCardsForChoiceId(choiceId, seed, stage);
        }

        return null;
    }
}