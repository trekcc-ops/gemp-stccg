package com.gempukku.stccg.draft;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;

import java.util.Random;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DraftPoolFilterPickDraftChoiceDefinition.class, name = "draftPoolFilterPick"),
        @JsonSubTypes.Type(value = DraftPoolFilterPluckDraftChoiceDefinition.class, name = "draftPoolFilterPluck"),
        @JsonSubTypes.Type(value = FilterPickDraftChoiceDefinition.class, name = "filterPick"),
        @JsonSubTypes.Type(value = MultipleCardPickDraftChoiceDefinition.class, name = "multipleCardPick"),
        @JsonSubTypes.Type(value = RandomSwitchDraftChoiceDefinition.class, name = "randomSwitch"),
        @JsonSubTypes.Type(value = SingleCollectionPickDraftChoiceDefinition.class, name = "singleCollectionPick"),
        @JsonSubTypes.Type(value = WeightedSwitchDraftChoiceDefinition.class, name = "weightedSwitch")
})
@JsonIgnoreProperties({ "repeat" })
public interface DraftChoiceDefinition {

    @JsonIgnore
    Iterable<DraftChoice> getDraftChoice(long seed, int stage, DefaultCardCollection draftPool);

    @JsonIgnore
    CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) throws InvalidDraftResultException;

    @JsonIgnore
    default Random getRandom(long seed, int stage) {
        return new Random(seed + (long) stage * 9497);
    }

}