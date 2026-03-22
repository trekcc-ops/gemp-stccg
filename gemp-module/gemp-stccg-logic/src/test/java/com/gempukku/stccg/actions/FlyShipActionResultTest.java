package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.movecard.FlyShipAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FlyShipActionResultTest extends AbstractAtTest {

    private ShipCard runabout;
    private MissionCard origin;
    private MissionCard destination;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        origin = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        destination = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        runabout = builder.addShipInSpace("101_331", "Runabout", P1, origin);
        builder.addCardAboardShipOrFacility("101_204", "Data", P1, runabout, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void serializeForYouTest() throws Exception {
        initializeGame();
        assertTrue(runabout.isAtSameLocationAsCard(origin));
        performAction(P1, FlyShipAction.class, runabout);
        assertTrue(runabout.isAtSameLocationAsCard(destination));

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode flyNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("FLEW_SHIP")) {
                flyNode = resultsNode.get(i);
                break;
            }
        }

        assertNotNull(flyNode);

        assertEquals(7, flyNode.size());

        assertTrue(flyNode.has("timestamp"));
        assertTrue(flyNode.has("resultId"));
        assertTrue(flyNode.has("type"));
        assertTrue(flyNode.has("performingPlayerId"));
        assertTrue(flyNode.has("targetCardId"));
        assertTrue(flyNode.has("originLocationId"));
        assertTrue(flyNode.has("destinationLocationId"));

        assertEquals(P1, flyNode.get("performingPlayerId").textValue());
        assertEquals(runabout.getCardId(), flyNode.get("targetCardId").intValue());
        assertEquals(origin.getLocationId(), flyNode.get("originLocationId").intValue());
        assertEquals(destination.getLocationId(), flyNode.get("destinationLocationId").intValue());
    }

    @Test
    public void serializeForOpponentTest() throws Exception {
        initializeGame();
        assertTrue(runabout.isAtSameLocationAsCard(origin));
        performAction(P1, FlyShipAction.class, runabout);
        assertTrue(runabout.isAtSameLocationAsCard(destination));

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode flyNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("FLEW_SHIP")) {
                flyNode = resultsNode.get(i);
                break;
            }
        }

        assertNotNull(flyNode);

        assertEquals(7, flyNode.size());

        assertTrue(flyNode.has("timestamp"));
        assertTrue(flyNode.has("resultId"));
        assertTrue(flyNode.has("type"));
        assertTrue(flyNode.has("performingPlayerId"));
        assertTrue(flyNode.has("targetCardId"));
        assertTrue(flyNode.has("originLocationId"));
        assertTrue(flyNode.has("destinationLocationId"));

        assertEquals(P1, flyNode.get("performingPlayerId").textValue());
        assertEquals(runabout.getCardId(), flyNode.get("targetCardId").intValue());
        assertEquals(origin.getLocationId(), flyNode.get("originLocationId").intValue());
        assertEquals(destination.getLocationId(), flyNode.get("destinationLocationId").intValue());
    }

}