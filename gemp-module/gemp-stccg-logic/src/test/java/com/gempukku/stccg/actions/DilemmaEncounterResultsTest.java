package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DilemmaEncounterResultsTest extends AbstractAtTest implements ActionResultTest {

    private MissionCard mission;
    private PhysicalCard punishmentBox;
    private PersonnelCard picard;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        mission = builder.addMission("101_154", "Excavation", P1);
        picard = builder.addCardOnPlanetSurface("101_215", "Jean-Luc Picard", P1, mission, PersonnelCard.class);
        punishmentBox = builder.addSeedCardUnderMission("112_031", "Punishment Box", P1, mission);

        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();
        attemptMission(P1, mission);

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode placeNode = null;
        JsonNode stopNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("DILEMMA_PLACED_ON_CARD")) {
                placeNode = resultsNode.get(i);
                stopNode = resultsNode.get(i+1);
                assertEquals("STOPPED_CARDS", stopNode.get("type").textValue());
                break;
            }
        }

        assertNotNull(placeNode);
        assertNotNull(stopNode);

        assertSerializedFields(placeNode, "targetCardId", "cardPlacedOnId");
        assertSerializedFields(stopNode, "targetCardIds");

        assertEquals(punishmentBox.getCardId(), placeNode.get("targetCardId").intValue());
        assertEquals(mission.getCardId(), placeNode.get("cardPlacedOnId").intValue());
        assertEquals(picard.getCardId(), stopNode.get("targetCardIds").get(0).intValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        attemptMission(P1, mission);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode placeNode = null;
        JsonNode stopNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("DILEMMA_PLACED_ON_CARD")) {
                placeNode = resultsNode.get(i);
                stopNode = resultsNode.get(i+1);
                assertEquals("STOPPED_CARDS", stopNode.get("type").textValue());
                break;
            }
        }

        assertNotNull(placeNode);
        assertNotNull(stopNode);

        assertSerializedFields(placeNode, "targetCardId", "cardPlacedOnId");
        assertSerializedFields(stopNode, "targetCardIds");

        assertEquals(punishmentBox.getCardId(), placeNode.get("targetCardId").intValue());
        assertEquals(mission.getCardId(), placeNode.get("cardPlacedOnId").intValue());
        assertEquals(picard.getCardId(), stopNode.get("targetCardIds").get(0).intValue());
    }

}