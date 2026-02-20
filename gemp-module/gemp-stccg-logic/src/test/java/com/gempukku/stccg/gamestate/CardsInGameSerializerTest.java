package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardsInGameSerializerTest extends AbstractAtTest {

    private final static String VERSION_NUMBER = "1.2.0";
    
    @Test
    public void dataForPlayerTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        builder.addMission("101_154", "Excavation", P2);
        MissionCard romulus = builder.addMission("101_147", "Cloaked Mission", P1);
        PhysicalCard gowron = builder.addCardOnPlanetSurface("101_261", "Gowron", P1, romulus);
        PhysicalCard armus1 = 
                builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P1, romulus);
        PhysicalCard armus2 = 
                builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, romulus);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.getGame();
        builder.startGame();

        /* There should be 65 cards in the game at this point:
            14 (7+7) in player hands
            46 (23+23) in player draw decks
            5 cited above - 2 missions, 2 dilemmas, and Gowron

            Each player should have their out of play cards (31) plus both missions and Gowron in "visibleCardsInGame"
         */

        assertEquals(65, _game.getGameState().getAllCardsInGame().size());
        assertEquals(7, _game.getGameState().getCardGroup(P1, Zone.HAND).size());
        assertEquals(7, _game.getGameState().getCardGroup(P2, Zone.HAND).size());
        assertEquals(23, _game.getGameState().getCardGroup(P1, Zone.DRAW_DECK).size());
        assertEquals(23, _game.getGameState().getCardGroup(P2, Zone.DRAW_DECK).size());

        JsonNode gameStateJson = new ObjectMapper().readTree(_game.getGameState().serializeForPlayer(P1));
        assertEquals(VERSION_NUMBER, gameStateJson.get("versionNumber").textValue());

        JsonNode cardsMap = gameStateJson.get("visibleCardsInGame");
        assertEquals(34, cardsMap.size());

        JsonNode gameStateJson2 = new ObjectMapper().readTree(_game.getGameState().serializeForPlayer(P2));
        assertEquals(VERSION_NUMBER, gameStateJson2.get("versionNumber").textValue());

        JsonNode cardsMap2 = gameStateJson.get("visibleCardsInGame");
        assertEquals(34, cardsMap2.size());

        /* Attempt mission. Gowron is killed and P2's Armus is removed from the game.
            This should not impact P2's visibleCardsInGame size, but P1's will add P2's Armus.
         */
        attemptMission(P1, romulus);
        assertTrue(personnelWasKilled((PersonnelCard) gowron));
        assertEquals(Zone.REMOVED, armus2.getZone());

        assertTrue(armus2.isKnownToPlayer(P1));
        assertFalse(armus1.isKnownToPlayer(P2));

        JsonNode newGameState1 = new ObjectMapper().readTree(_game.getGameState().serializeForPlayer(P1));
        assertEquals(35, newGameState1.get("visibleCardsInGame").size());

        JsonNode newGameState2 = new ObjectMapper().readTree(_game.getGameState().serializeForPlayer(P2));
        assertEquals(34, newGameState2.get("visibleCardsInGame").size());
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
        builder.setPhase(Phase.CARD_PLAY);
        _game = builder.getGame();
        builder.startGame();

        // All 64 cards should be in the "cardsInGame" map
        assertEquals(64, _game.getGameState().getAllCardsInGame().size());
        assertEquals(7, _game.getGameState().getCardGroup(P1, Zone.HAND).size());
        assertEquals(7, _game.getGameState().getCardGroup(P2, Zone.HAND).size());
        assertEquals(23, _game.getGameState().getCardGroup(P1, Zone.DRAW_DECK).size());
        assertEquals(23, _game.getGameState().getCardGroup(P2, Zone.DRAW_DECK).size());

        JsonNode gameStateJson = new ObjectMapper().readTree(_game.getGameState().serializeComplete());
        assertEquals(VERSION_NUMBER, gameStateJson.get("versionNumber").textValue());
        JsonNode cardsMap = gameStateJson.get("cardsInGame");
        assertEquals(64, cardsMap.size());
    }

}