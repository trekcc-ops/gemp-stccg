package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;

public class PhysicalCardDeserializer {

    public static void deserialize(PhysicalCard card, JsonNode node) throws CardNotFoundException {

        DefaultGame game = card.getGame();

        Zone zone = Zone.valueOf(node.get("zone").textValue());
        card.setZone(zone);

        if (card instanceof AffiliatedCard affiliatedCard) {
            Affiliation affiliation = Affiliation.valueOf(node.get("affiliation").textValue());
            affiliatedCard.setCurrentAffiliationWithImage(affiliation);
        }

        if (node.has("locationZoneIndex") && game instanceof ST1EGame st1eGame) {
            int locationZoneIndex = node.get("locationZoneIndex").intValue();
            ST1ELocation location = st1eGame.getGameState().getSpacelineLocations().get(locationZoneIndex);
            card.setLocation(location);
        }

        if (card instanceof MissionCard mission)
            mission.setCompleted(node.get("completed").booleanValue());
        if (card instanceof PhysicalShipCard shipCard)
            shipCard._rangeAvailable = node.get("rangeAvailable").intValue();

        // whileInZoneData
    }

}