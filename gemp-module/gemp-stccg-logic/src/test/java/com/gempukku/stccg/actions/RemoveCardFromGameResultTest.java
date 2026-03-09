package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class RemoveCardFromGameResultTest extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard negotiations;
    private Collection<PersonnelCard> attemptingPersonnel;

    private void initializeGame()
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        attemptingPersonnel = new ArrayList<>();
        _mission = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        ShipCard runabout = builder.addShipInSpace("101_331", "Runabout", P1, _mission);
        PersonnelCard sarek = builder.addCardAboardShipOrFacility("101_233", "Sarek", P1, runabout, PersonnelCard.class);
        attemptingPersonnel.add(sarek);
        PersonnelCard wallace = builder.addCardAboardShipOrFacility("101_203", "Darian Wallace", P1, runabout, PersonnelCard.class);
        attemptingPersonnel.add(wallace);

        negotiations = builder.addSeedCardUnderMission("155_012", "Tense Negotiations", P2, _mission);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();
        assertEquals(2, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        // Verify dilemma was nullified
        assertEquals(Zone.REMOVED, negotiations.getZone());
        assertTrue(cardWasNullified(negotiations));

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode removeNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("REMOVED_CARD_FROM_GAME")) {
                removeNode = resultsNode.get(i);
                assertEquals("NULLIFIED", resultsNode.get(i-1).get("type").textValue());
                break;
            }
        }

        assertNotNull(removeNode);

        assertEquals(5, removeNode.size());

        assertTrue(removeNode.has("timestamp"));
        assertTrue(removeNode.has("resultId"));
        assertTrue(removeNode.has("type"));
        assertTrue(removeNode.has("performingPlayerId"));
        assertTrue(removeNode.has("targetCardIds"));

        assertEquals("REMOVED_CARD_FROM_GAME", removeNode.get("type").textValue());
        assertEquals(P1, removeNode.get("performingPlayerId").textValue());
        assertTrue(jsonListIsCardIds(removeNode.get("targetCardIds"), List.of(negotiations)));
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        assertEquals(2, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        // Verify dilemma was nullified
        assertEquals(Zone.REMOVED, negotiations.getZone());
        assertTrue(cardWasNullified(negotiations));

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode removeNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("REMOVED_CARD_FROM_GAME")) {
                removeNode = resultsNode.get(i);
                assertEquals("NULLIFIED", resultsNode.get(i-1).get("type").textValue());
                break;
            }
        }

        assertNotNull(removeNode);

        assertEquals(5, removeNode.size());

        assertTrue(removeNode.has("timestamp"));
        assertTrue(removeNode.has("resultId"));
        assertTrue(removeNode.has("type"));
        assertTrue(removeNode.has("performingPlayerId"));
        assertTrue(removeNode.has("targetCardIds"));

        assertEquals("REMOVED_CARD_FROM_GAME", removeNode.get("type").textValue());
        assertEquals(P1, removeNode.get("performingPlayerId").textValue());
        assertTrue(jsonListIsCardIds(removeNode.get("targetCardIds"), List.of(negotiations)));
    }

}