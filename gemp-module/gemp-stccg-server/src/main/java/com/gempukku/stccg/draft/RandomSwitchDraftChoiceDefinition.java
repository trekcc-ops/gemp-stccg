package com.gempukku.stccg.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;

import java.util.List;

public class RandomSwitchDraftChoiceDefinition implements DraftChoiceDefinition {

    private final List<DraftChoiceDefinition> _draftChoiceDefinitionList;

    public RandomSwitchDraftChoiceDefinition(
            @JsonProperty(value = "switchResult", required = true)
            List<DraftChoiceDefinition> definitionList
    ) {
        _draftChoiceDefinitionList = definitionList;
    }


    @Override
    public Iterable<DraftChoice> getDraftChoice(long seed, int stage,
                                                DefaultCardCollection draftPool) {
        return TextUtils.getRandomItemsFromList(_draftChoiceDefinitionList, getRandom(seed, stage))
                .getDraftChoice(seed, stage, draftPool);
    }

    @Override
    public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage)
            throws InvalidDraftResultException {
        return TextUtils.getRandomItemsFromList(_draftChoiceDefinitionList, getRandom(seed, stage))
                .getCardsForChoiceId(choiceId, seed, stage);
    }

}