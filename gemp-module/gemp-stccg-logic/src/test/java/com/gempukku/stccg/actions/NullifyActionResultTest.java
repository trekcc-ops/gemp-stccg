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

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class NullifyActionResultTest extends AbstractAtTest {

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
    public void nullifyDilemmaTest() throws Exception {
        initializeGame();
        assertEquals(2, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        // Verify dilemma was nullified
        assertEquals(Zone.REMOVED, negotiations.getZone());
        assertTrue(cardWasNullified(negotiations));

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode nullifyNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("NULLIFY")) {
                nullifyNode = resultsNode.get(i);
                break;
            }
        }

        assertNotNull(nullifyNode);

        assertEquals(6, nullifyNode.size());

        assertTrue(nullifyNode.has("timestamp"));
        assertTrue(nullifyNode.has("resultId"));
        assertTrue(nullifyNode.has("type"));
        assertTrue(nullifyNode.has("performingPlayerId"));
        assertTrue(nullifyNode.has("targetCardId"));
        assertTrue(nullifyNode.has("performingCardId"));

        assertEquals("NULLIFY", nullifyNode.get("type").textValue());
        assertEquals(P1, nullifyNode.get("performingPlayerId").textValue());
        assertEquals(negotiations.getCardId(), nullifyNode.get("targetCardId").intValue());
        assertEquals(negotiations.getCardId(), nullifyNode.get("performingCardId").intValue());
    }

}