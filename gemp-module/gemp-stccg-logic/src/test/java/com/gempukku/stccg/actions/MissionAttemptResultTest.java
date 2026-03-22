package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MissionAttemptResultTest extends AbstractAtTest implements ActionResultTest {

    private MissionCard mission;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        mission = builder.addMission("101_154", "Excavation", P1);
        builder.addCardOnPlanetSurface("101_215", "Jean-Luc Picard", P1, mission, PersonnelCard.class);

        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();
        attemptMission(P1, mission);

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode startNode = null;
        JsonNode endNode = null;
        JsonNode pointsNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("MISSION_ATTEMPT_STARTED")) {
                startNode = resultsNode.get(i);
                endNode = resultsNode.get(i+1);
                pointsNode = resultsNode.get(i+2);
                assertEquals("MISSION_ATTEMPT_ENDED", endNode.get("type").textValue());
                assertEquals("SCORED_POINTS", pointsNode.get("type").textValue());
                break;
            }
        }

        assertNotNull(startNode);
        assertNotNull(endNode);
        assertNotNull(pointsNode);

        assertSerializedFields(startNode, "targetCardId");
        assertSerializedFields(endNode, "targetCardId", "wasSuccessful");
        assertSerializedFields(pointsNode, "performingCardId", "pointsScored", "pointsAreBonus");

        assertEquals(mission.getCardId(), startNode.get("targetCardId").intValue());
        assertEquals(mission.getCardId(), endNode.get("targetCardId").intValue());
        assertTrue(endNode.get("wasSuccessful").booleanValue());

        assertEquals(mission.getCardId(), pointsNode.get("performingCardId").intValue());
        assertEquals(25, pointsNode.get("pointsScored").intValue());
        assertFalse(pointsNode.get("pointsAreBonus").booleanValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        attemptMission(P1, mission);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode startNode = null;
        JsonNode endNode = null;
        JsonNode pointsNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("MISSION_ATTEMPT_STARTED")) {
                startNode = resultsNode.get(i);
                endNode = resultsNode.get(i+1);
                pointsNode = resultsNode.get(i+2);
                assertEquals("MISSION_ATTEMPT_ENDED", endNode.get("type").textValue());
                assertEquals("SCORED_POINTS", pointsNode.get("type").textValue());
                break;
            }
        }

        assertNotNull(startNode);
        assertNotNull(endNode);
        assertNotNull(pointsNode);

        assertSerializedFields(startNode, "targetCardId");
        assertSerializedFields(endNode, "targetCardId", "wasSuccessful");
        assertSerializedFields(pointsNode, "performingCardId", "pointsScored", "pointsAreBonus");

        assertEquals(mission.getCardId(), startNode.get("targetCardId").intValue());
        assertEquals(mission.getCardId(), endNode.get("targetCardId").intValue());
        assertTrue(endNode.get("wasSuccessful").booleanValue());

        assertEquals(mission.getCardId(), pointsNode.get("performingCardId").intValue());
        assertEquals(25, pointsNode.get("pointsScored").intValue());
        assertFalse(pointsNode.get("pointsAreBonus").booleanValue());
    }

}