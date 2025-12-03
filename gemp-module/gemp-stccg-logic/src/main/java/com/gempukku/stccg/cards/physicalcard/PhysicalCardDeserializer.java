package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

public class PhysicalCardDeserializer {

    public static void deserialize(ST1EGame game, PhysicalCard card, JsonNode node) throws CardNotFoundException {

        if (card instanceof AffiliatedCard affiliatedCard && node.has("affiliation")) {
            Affiliation affiliation = Affiliation.valueOf(node.get("affiliation").textValue());
            affiliatedCard.setCurrentAffiliation(affiliation);
        }

        if (node.has("locationZoneIndex")) {
            int locationZoneIndex = node.get("locationZoneIndex").intValue();
            MissionLocation location = game.getGameState().getSpacelineLocations().get(locationZoneIndex);
            card.setLocation(game, location);
        }

        if (card instanceof PhysicalShipCard shipCard)
            shipCard._rangeAvailable = node.get("rangeAvailable").intValue();

        // whileInZoneData
    }

}