package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DiscardActionResultTest extends AbstractAtTest {

    private MissionCard mission;
    private PersonnelCard worf;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, mission);
        worf = builder.addCardOnPlanetSurface("101_251", "Worf", P1, mission, PersonnelCard.class);

        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();
        attemptMission(P1, mission);

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode discardWorfNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("DISCARD")) {
                discardWorfNode = resultsNode.get(i);
                assertEquals("KILL", resultsNode.get(i - 1).get("type").textValue());
                break;
            }
        }

        assertNotNull(discardWorfNode);

        assertEquals(6, discardWorfNode.size());

        assertTrue(discardWorfNode.has("timestamp"));
        assertTrue(discardWorfNode.has("resultId"));
        assertTrue(discardWorfNode.has("type"));
        assertTrue(discardWorfNode.has("performingPlayerId"));
        assertTrue(discardWorfNode.has("targetCardId"));
        assertTrue(discardWorfNode.has("destination"));

        assertEquals("DISCARD", discardWorfNode.get("type").textValue());
        assertEquals(P1, discardWorfNode.get("performingPlayerId").textValue());
        assertEquals(worf.getCardId(), discardWorfNode.get("targetCardId").intValue());
        assertEquals("DISCARD", discardWorfNode.get("destination").textValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        attemptMission(P1, mission);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode discardWorfNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("DISCARD")) {
                discardWorfNode = resultsNode.get(i);
                assertEquals("KILL", resultsNode.get(i - 1).get("type").textValue());
                break;
            }
        }

        assertNotNull(discardWorfNode);

        assertEquals(6, discardWorfNode.size());

        assertTrue(discardWorfNode.has("timestamp"));
        assertTrue(discardWorfNode.has("resultId"));
        assertTrue(discardWorfNode.has("type"));
        assertTrue(discardWorfNode.has("performingPlayerId"));
        assertTrue(discardWorfNode.has("targetCardId"));
        assertTrue(discardWorfNode.has("destination"));

        assertEquals("DISCARD", discardWorfNode.get("type").textValue());
        assertEquals(P1, discardWorfNode.get("performingPlayerId").textValue());
        assertEquals(worf.getCardId(), discardWorfNode.get("targetCardId").intValue());
        assertEquals("DISCARD", discardWorfNode.get("destination").textValue());
    }

}