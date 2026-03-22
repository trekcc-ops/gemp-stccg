package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlaceCardInPointAreaResultTest extends AbstractAtTest implements ActionResultTest {

    private MissionCard mission;
    private PhysicalCard scientificDiplomacy;
    private PersonnelCard reyga;
    private PhysicalCard taitt;

    @SuppressWarnings("SpellCheckingInspection")
    public void initializeGame()
            throws CardNotFoundException, DecisionResultInvalidException, InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("155_039", "Host Metaphasic Shielding Test", P1);
        builder.addCardToCoreAsSeeded("155_022", "Continuing Mission", P1); // to get TNG icon
        scientificDiplomacy = builder.addCardInHand("155_030", "Scientific Diplomacy", P1);

        // Put some SCIENCE in discard
        taitt = builder.addCardInDiscard("101_242", "Taitt", P1);

        ShipCard mercShip = builder.addShipInSpace("101_354", "Mercenary Ship", P1, mission);
        builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, mercShip, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_290", "Baran", P1, mercShip, PersonnelCard.class);
        reyga = builder.addCardInHand("138_034", "Dr. Reyga", P1, PersonnelCard.class);

        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }
    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();
        playCard(P1, scientificDiplomacy);
        playCard(P1, reyga);
        skipPhase(Phase.CARD_PLAY);

        attemptMission(P1, mission);
        assertTrue(mission.isCompleted(_game));

        useGameText(P1, scientificDiplomacy);

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode placeNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("PLACED_CARD_IN_POINT_AREA")) {
                placeNode = resultsNode.get(i);
                break;
            }
        }

        assertNotNull(placeNode);
        assertSerializedFields(placeNode, "targetCardId");
        assertEquals(taitt.getCardId(), placeNode.get("targetCardId").intValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        playCard(P1, scientificDiplomacy);
        playCard(P1, reyga);
        skipPhase(Phase.CARD_PLAY);

        attemptMission(P1, mission);
        assertTrue(mission.isCompleted(_game));

        useGameText(P1, scientificDiplomacy);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode placeNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("PLACED_CARD_IN_POINT_AREA")) {
                placeNode = resultsNode.get(i);
                break;
            }
        }

        assertNotNull(placeNode);
        assertSerializedFields(placeNode, "targetCardId");
        assertEquals(taitt.getCardId(), placeNode.get("targetCardId").intValue());
    }

}