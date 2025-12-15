package com.gempukku.stccg.packs;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.GenericCardItem;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BoosterPack.class, name = "booster"),
        @JsonSubTypes.Type(value = FixedPackBox.class, name = "pack"),
        @JsonSubTypes.Type(value = UnweightedRandomPack.class, name = "unweightedRandom"),
        @JsonSubTypes.Type(value = WeightedRandomPack.class, name = "weightedRandom")
})
public interface PackBox {
    List<GenericCardItem> openPack(CardBlueprintLibrary library);

    String getName();

}