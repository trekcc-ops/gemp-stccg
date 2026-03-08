package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.movecard.DockAction;
import com.gempukku.stccg.actions.movecard.UndockAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DockingActionResultTest extends AbstractAtTest {

    private ShipCard runabout;
    private FacilityCard outpost;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        runabout = builder.addDockedShip("101_331", "Runabout", P1, outpost);
        builder.addCardAboardShipOrFacility("101_204", "Data", P1, runabout, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void serializeForYouTest() throws Exception {
        initializeGame();
        performAction(P1, UndockAction.class, runabout);
        assertFalse(runabout.isDocked());
        performAction(P1, DockAction.class, runabout);
        assertTrue(runabout.isDocked());

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode dockNode = null;
        JsonNode undockNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("DOCK_SHIP")) {
                dockNode = resultsNode.get(i);
                break;
            }
        }

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("UNDOCK_SHIP")) {
                undockNode = resultsNode.get(i);
                break;
            }
        }

        assertNotNull(dockNode);
        assertNotNull(undockNode);

        assertEquals(dockNode.get("resultId").intValue(), undockNode.get("resultId").intValue() + 1);

        assertEquals(6, dockNode.size());
        assertEquals(6, undockNode.size());

        assertTrue(dockNode.has("timestamp"));
        assertTrue(dockNode.has("resultId"));
        assertTrue(dockNode.has("type"));
        assertTrue(dockNode.has("performingPlayerId"));
        assertTrue(dockNode.has("targetCardId"));
        assertTrue(dockNode.has("dockedAtCardId"));

        assertEquals(P1, dockNode.get("performingPlayerId").textValue());
        assertEquals(runabout.getCardId(), dockNode.get("targetCardId").intValue());
        assertEquals(outpost.getCardId(), dockNode.get("dockedAtCardId").intValue());

        assertEquals(P1, undockNode.get("performingPlayerId").textValue());
        assertEquals(runabout.getCardId(), undockNode.get("targetCardId").intValue());
        assertEquals(outpost.getCardId(), undockNode.get("undockingFromCardId").intValue());
    }

    @Test
    public void serializeForOpponentTest() throws Exception {
        initializeGame();
        performAction(P1, UndockAction.class, runabout);
        assertFalse(runabout.isDocked());
        performAction(P1, DockAction.class, runabout);
        assertTrue(runabout.isDocked());

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode dockNode = null;
        JsonNode undockNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("DOCK_SHIP")) {
                dockNode = resultsNode.get(i);
                break;
            }
        }

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("UNDOCK_SHIP")) {
                undockNode = resultsNode.get(i);
                break;
            }
        }

        assertNotNull(dockNode);
        assertNotNull(undockNode);

        assertEquals(dockNode.get("resultId").intValue(), undockNode.get("resultId").intValue() + 1);

        assertEquals(6, dockNode.size());
        assertEquals(6, undockNode.size());

        assertTrue(dockNode.has("timestamp"));
        assertTrue(dockNode.has("resultId"));
        assertTrue(dockNode.has("type"));
        assertTrue(dockNode.has("performingPlayerId"));
        assertTrue(dockNode.has("targetCardId"));
        assertTrue(dockNode.has("dockedAtCardId"));

        assertEquals(P1, dockNode.get("performingPlayerId").textValue());
        assertEquals(runabout.getCardId(), dockNode.get("targetCardId").intValue());
        assertEquals(outpost.getCardId(), dockNode.get("dockedAtCardId").intValue());

        assertEquals(P1, undockNode.get("performingPlayerId").textValue());
        assertEquals(runabout.getCardId(), undockNode.get("targetCardId").intValue());
        assertEquals(outpost.getCardId(), undockNode.get("undockingFromCardId").intValue());
    }


}