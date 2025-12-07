package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;

public class EquipmentCard extends ST1EPhysicalCard implements CardWithCompatibility, ReportableCard {

    public EquipmentCard(int cardId, String ownerName, CardBlueprint blueprint) {
        super(cardId, ownerName, blueprint);
    }


}