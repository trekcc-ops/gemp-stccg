package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KillActionResultTest extends AbstractAtTest {

    private MissionCard mission;
    private PersonnelCard worf;
    private PhysicalCard armus;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        armus = builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, mission);
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

        JsonNode killWorfNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("KILL")) {
                killWorfNode = resultsNode.get(i);
                assertEquals("DISCARD", resultsNode.get(i + 1).get("type").textValue());
                break;
            }
        }

        assertNotNull(killWorfNode);

        assertEquals(6, killWorfNode.size());

        assertTrue(killWorfNode.has("timestamp"));
        assertTrue(killWorfNode.has("resultId"));
        assertTrue(killWorfNode.has("type"));
        assertTrue(killWorfNode.has("performingPlayerId"));
        assertTrue(killWorfNode.has("killedCardIds"));
        assertTrue(killWorfNode.has("performingCardId"));

        assertEquals("KILL", killWorfNode.get("type").textValue());
        assertEquals(P2, killWorfNode.get("performingPlayerId").textValue());
        assertTrue(jsonListIsCardIds(killWorfNode.get("killedCardIds"), List.of(worf)));
        assertEquals(armus.getCardId(), killWorfNode.get("performingCardId").intValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        attemptMission(P1, mission);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode killWorfNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("KILL")) {
                killWorfNode = resultsNode.get(i);
                assertEquals("DISCARD", resultsNode.get(i + 1).get("type").textValue());
                break;
            }
        }

        assertNotNull(killWorfNode);

        assertEquals(6, killWorfNode.size());

        assertTrue(killWorfNode.has("timestamp"));
        assertTrue(killWorfNode.has("resultId"));
        assertTrue(killWorfNode.has("type"));
        assertTrue(killWorfNode.has("performingPlayerId"));
        assertTrue(killWorfNode.has("killedCardIds"));
        assertTrue(killWorfNode.has("performingCardId"));

        assertEquals("KILL", killWorfNode.get("type").textValue());
        assertEquals(P2, killWorfNode.get("performingPlayerId").textValue());
        assertTrue(jsonListIsCardIds(killWorfNode.get("killedCardIds"), List.of(worf)));
        assertEquals(armus.getCardId(), killWorfNode.get("performingCardId").intValue());
    }

}