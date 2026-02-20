package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameStateSerializerTest extends AbstractAtTest {

    private final static String VERSION_NUMBER = "1.2.0";

    @Test
    public void serializeCompleteTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.startGame();

        JsonNode gameStateJson = new ObjectMapper().readTree(_game.getGameState().serializeComplete());
        assertEquals(16, gameStateJson.size());
        assertTrue(gameStateJson.has("currentPhase"));
        assertTrue(gameStateJson.has("phasesInOrder"));
        assertTrue(gameStateJson.has("currentProcess"));
        assertTrue(gameStateJson.has("playerOrder"));
        assertTrue(gameStateJson.has("cardsInGame"));
        assertTrue(gameStateJson.has("playerMap"));
        assertTrue(gameStateJson.has("spacelineLocations"));
        assertTrue(gameStateJson.has("awayTeams"));
        assertTrue(gameStateJson.has("actions"));
        assertTrue(gameStateJson.has("performedActions"));
        assertTrue(gameStateJson.has("playerClocks"));
        assertTrue(gameStateJson.has("actionLimits"));
        assertTrue(gameStateJson.has("modifiers"));
        assertTrue(gameStateJson.has("gameLocations"));
        assertTrue(gameStateJson.has("spacelineElements"));
        assertTrue(gameStateJson.has("versionNumber"));

        assertEquals(VERSION_NUMBER, gameStateJson.get("versionNumber").textValue());
    }

    @Test
    public void serializeForPlayerTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.startGame();

        JsonNode gameStateJson = new ObjectMapper().readTree(_game.getGameState().serializeForPlayer(P1));
        assertEquals(14, gameStateJson.size());
        assertTrue(gameStateJson.has("requestingPlayer"));
        assertTrue(gameStateJson.has("currentPhase"));
        assertTrue(gameStateJson.has("phasesInOrder"));
        assertTrue(gameStateJson.has("playerOrder"));
        assertTrue(gameStateJson.has("visibleCardsInGame"));
        assertTrue(gameStateJson.has("playerMap"));
        assertTrue(gameStateJson.has("spacelineLocations"));
        assertTrue(gameStateJson.has("awayTeams"));
        assertTrue(gameStateJson.has("performedActions"));
        assertTrue(gameStateJson.has("playerClocks"));
        assertTrue(gameStateJson.has("pendingDecision"));
        assertTrue(gameStateJson.has("gameLocations"));
        assertTrue(gameStateJson.has("spacelineElements"));
        assertTrue(gameStateJson.has("versionNumber"));

        assertEquals(VERSION_NUMBER, gameStateJson.get("versionNumber").textValue());
    }

    @Test
    public void spacelineElementsTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        builder.addMission("101_154", "Excavation", P1);
        _game = builder.getGame();
        builder.startGame();

        JsonNode gameStateJson = new ObjectMapper().readTree(_game.getGameState().serializeForPlayer(P1));
        JsonNode elementsJson = gameStateJson.get("spacelineElements");
        assertTrue(elementsJson.isArray());
        assertEquals(1, elementsJson.size());
        JsonNode elementNode = elementsJson.get(0);
        assertEquals(3, elementNode.size());
        assertEquals("location", elementNode.get("type").textValue());
        assertEquals(1, elementNode.get("locationId").intValue());
        assertEquals("ALPHA", elementNode.get("quadrant").textValue());
    }


}