package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MissionLocationSerializerTest extends AbstractAtTest {

    private final static String VERSION_NUMBER = "1.2.0";
    
    @Test
    public void dataForPlayerTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        builder.addMission("101_154", "Excavation", P1);
        MissionCard romulus = builder.addMission("101_147", "Cloaked Mission", P2);
        PhysicalCard armus1 = 
                builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P1, romulus);
        PhysicalCard armus2 = 
                builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, romulus);
        _game = builder.getGame();
        builder.startGame();

        JsonNode gameStateJson = new ObjectMapper().readTree(_game.getGameState().serializeForPlayer(P1));
        assertEquals(VERSION_NUMBER, gameStateJson.get("versionNumber").textValue());

        JsonNode jsonArray = gameStateJson.get("spacelineLocations");
        assertTrue(jsonArray.isArray());
        assertEquals(2, jsonArray.size());

        JsonNode kurlNode = jsonArray.get(0);
        assertEquals(7, kurlNode.size());
        assertEquals(1, kurlNode.get("locationId").intValue());
        assertEquals("ALPHA", kurlNode.get("quadrant").textValue());
        assertEquals("Kurl", kurlNode.get("locationName").textValue());
        assertFalse(kurlNode.get("isCompleted").booleanValue());
        assertFalse(kurlNode.get("isHomeworld").booleanValue());
        assertEquals(1, kurlNode.get("missionCardIds").size());
        assertEquals(0, kurlNode.get("seedCardCount").intValue());

        JsonNode romulusNode = jsonArray.get(1);
        assertEquals(8, romulusNode.size()); // Has an extra property for "region"
        assertEquals(2, romulusNode.get("locationId").intValue());
        assertEquals("ALPHA", romulusNode.get("quadrant").textValue());
        assertEquals("ROMULUS_SYSTEM", romulusNode.get("region").textValue());
        assertEquals("Romulus", romulusNode.get("locationName").textValue());
        assertFalse(romulusNode.get("isCompleted").booleanValue());
        assertFalse(romulusNode.get("isHomeworld").booleanValue());
        assertEquals(1, romulusNode.get("missionCardIds").size());
        assertEquals(2, romulusNode.get("seedCardCount").intValue());
    }

    @Test
    public void dataForAdminTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        builder.addMission("101_154", "Excavation", P1);
        MissionCard romulus = builder.addMission("101_147", "Cloaked Mission", P2);
        PhysicalCard armus1 =
                builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P1, romulus);
        PhysicalCard armus2 =
                builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, romulus);
        _game = builder.getGame();
        builder.startGame();

        JsonNode gameStateJson = new ObjectMapper().readTree(_game.getGameState().serializeComplete());
        assertEquals(VERSION_NUMBER, gameStateJson.get("versionNumber").textValue());

        JsonNode jsonArray = gameStateJson.get("spacelineLocations");
        assertTrue(jsonArray.isArray());
        assertEquals(2, jsonArray.size());

        JsonNode kurlNode = jsonArray.get(0);
        assertEquals(8, kurlNode.size());
        assertEquals(1, kurlNode.get("locationId").intValue());
        assertEquals("ALPHA", kurlNode.get("quadrant").textValue());
        assertEquals("Kurl", kurlNode.get("locationName").textValue());
        assertFalse(kurlNode.get("isCompleted").booleanValue());
        assertFalse(kurlNode.get("isHomeworld").booleanValue());
        assertEquals(1, kurlNode.get("missionCardIds").size());
        assertEquals(0, kurlNode.get("seedCardCount").intValue());
        assertTrue(kurlNode.has("seedCardIds"));
        assertEquals(0, kurlNode.get("seedCardIds").size());

        JsonNode romulusNode = jsonArray.get(1);
        assertEquals(9, romulusNode.size()); // Has an extra property for "region"
        assertEquals(2, romulusNode.get("locationId").intValue());
        assertEquals("ALPHA", romulusNode.get("quadrant").textValue());
        assertEquals("ROMULUS_SYSTEM", romulusNode.get("region").textValue());
        assertEquals("Romulus", romulusNode.get("locationName").textValue());
        assertFalse(romulusNode.get("isCompleted").booleanValue());
        assertFalse(romulusNode.get("isHomeworld").booleanValue());
        assertEquals(1, romulusNode.get("missionCardIds").size());
        assertEquals(2, romulusNode.get("seedCardCount").intValue());
        assertTrue(romulusNode.has("seedCardIds"));
        assertEquals(2, romulusNode.get("seedCardIds").size());
    }



}