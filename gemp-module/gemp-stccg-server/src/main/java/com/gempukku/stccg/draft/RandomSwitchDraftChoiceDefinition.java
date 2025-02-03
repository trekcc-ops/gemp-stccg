package com.gempukku.stccg.draft;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.common.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomSwitchDraftChoiceDefinition implements DraftChoiceDefinition {

    private final List<DraftChoiceDefinition> _draftChoiceDefinitionList = new ArrayList<>();

    public RandomSwitchDraftChoiceDefinition(JsonNode node, SoloDraftDefinitions builder) {

        for (JsonNode switchResultObject : JsonUtils.toArray(node.get("switchResult")))
            _draftChoiceDefinitionList.add(builder.buildDraftChoiceDefinition(switchResultObject));
    }

    @Override
    public Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage,
                                                          DefaultCardCollection draftPool) {
        return TextUtils.getRandomItemsFromList(_draftChoiceDefinitionList, getRandom(seed, stage))
                .getDraftChoice(seed, stage, draftPool);
    }

    @Override
    public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
        return TextUtils.getRandomItemsFromList(_draftChoiceDefinitionList, getRandom(seed, stage))
                .getCardsForChoiceId(choiceId, seed, stage);
    }

}