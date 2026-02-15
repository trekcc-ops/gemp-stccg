package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;

@JsonIgnoreProperties(value = { "cardType", "hasUniversalIcon", "imageUrl", "isInPlay", "title", "uniqueness" },
        allowGetters = true)
public class EquipmentCard extends ST1EPhysicalCard implements CardWithCompatibility, ReportableCard {

    @JsonCreator
    public EquipmentCard(
            @JsonProperty("cardId")
            int cardId,
            @JsonProperty("owner")
            String ownerName,
            @JsonProperty("blueprintId")
            String blueprintId,
            @JacksonInject
            CardBlueprintLibrary blueprintLibrary) throws CardNotFoundException {
        super(cardId, ownerName, blueprintLibrary.getCardBlueprint(blueprintId));
    }


    public EquipmentCard(int cardId, String ownerName, CardBlueprint blueprint) {
        super(cardId, ownerName, blueprint);
    }


}