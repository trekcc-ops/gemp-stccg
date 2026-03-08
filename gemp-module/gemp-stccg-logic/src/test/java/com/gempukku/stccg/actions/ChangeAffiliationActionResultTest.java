package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.modifiers.ChangeAffiliationAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChangeAffiliationActionResultTest extends AbstractAtTest {

    private PersonnelCard gareb;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        MissionCard mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P2);
        gareb = builder.addCardOnPlanetSurface("141_037", "Gareb", P1, mission, PersonnelCard.class,
                Affiliation.ROMULAN);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();
        assertTrue(gareb.hasAffiliation(_game, Affiliation.ROMULAN, P1));
        Action action = performAction(P1, ChangeAffiliationAction.class, gareb);
        assertTrue(gareb.hasAffiliation(_game, Affiliation.NON_ALIGNED, P1));

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");
        JsonNode changeNode = resultsNode.get(resultsNode.size() - 1);
        assertEquals(6, changeNode.size());

        assertTrue(changeNode.has("timestamp"));
        assertTrue(changeNode.has("targetCardId"));
        assertTrue(changeNode.has("resultId"));
        assertTrue(changeNode.has("type"));
        assertTrue(changeNode.has("performingPlayerId"));
        assertTrue(changeNode.has("newAffiliation"));

        assertEquals("CHANGE_AFFILIATION", changeNode.get("type").textValue());
        assertEquals(P1, changeNode.get("performingPlayerId").textValue());
        assertEquals(gareb.getCardId(), changeNode.get("targetCardId").intValue());
        assertEquals("NON_ALIGNED", changeNode.get("newAffiliation").textValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        Action action = performAction(P1, ChangeAffiliationAction.class, gareb);
        assertTrue(gareb.hasAffiliation(_game, Affiliation.NON_ALIGNED, P2));

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");
        JsonNode changeNode = resultsNode.get(resultsNode.size() - 1);
        assertEquals(6, changeNode.size());

        assertTrue(changeNode.has("timestamp"));
        assertTrue(changeNode.has("targetCardId"));
        assertTrue(changeNode.has("resultId"));
        assertTrue(changeNode.has("type"));
        assertTrue(changeNode.has("performingPlayerId"));
        assertTrue(changeNode.has("newAffiliation"));

        assertEquals("CHANGE_AFFILIATION", changeNode.get("type").textValue());
        assertEquals(P1, changeNode.get("performingPlayerId").textValue());
        assertEquals(gareb.getCardId(), changeNode.get("targetCardId").intValue());
        assertEquals("NON_ALIGNED", changeNode.get("newAffiliation").textValue());
    }

}