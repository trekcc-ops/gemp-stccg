package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WalkCardsActionResultTest extends AbstractAtTest {

    private MissionCard mission;
    private List<PersonnelCard> cardsToWalk;
    private FacilityCard outpost;
    private ShipCard runabout;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P2);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1, mission);
        runabout = builder.addDockedShip("101_331", "Runabout", P1, outpost);

        cardsToWalk = new ArrayList<>();
        cardsToWalk.add(builder.addCardAboardShipOrFacility("101_242", "Taitt", P1, outpost, PersonnelCard.class));
        cardsToWalk.add(builder.addCardAboardShipOrFacility("101_242", "Taitt", P1, outpost, PersonnelCard.class));
        cardsToWalk.add(builder.addCardAboardShipOrFacility("101_242", "Taitt", P1, outpost, PersonnelCard.class));
        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();
        Action walkAction = walkCards(P1, outpost, cardsToWalk, runabout);

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");
        JsonNode walkNode = resultsNode.get(resultsNode.size() - 1);
        assertEquals(7, walkNode.size());

        assertTrue(walkNode.has("timestamp"));
        assertTrue(walkNode.has("targetCardIds"));
        assertTrue(walkNode.has("resultId"));
        assertTrue(walkNode.has("type"));
        assertTrue(walkNode.has("performingPlayerId"));
        assertTrue(walkNode.has("destinationCardId"));
        assertTrue(walkNode.has("originCardId"));

        assertEquals("WALK_CARDS", walkNode.get("type").textValue());
        assertEquals(P1, walkNode.get("performingPlayerId").textValue());
        assertTrue(jsonListIsCardIds(walkNode.get("targetCardIds"), cardsToWalk));
        assertEquals(outpost.getCardId(), walkNode.get("originCardId").intValue());
        assertEquals(runabout.getCardId(), walkNode.get("destinationCardId").intValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        Action beamAction = walkCards(P1, outpost, cardsToWalk, runabout);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");
        JsonNode walkNode = resultsNode.get(resultsNode.size() - 1);
        assertEquals(7, walkNode.size());

        assertTrue(walkNode.has("timestamp"));
        assertTrue(walkNode.has("targetCardIds"));
        assertTrue(walkNode.has("resultId"));
        assertTrue(walkNode.has("type"));
        assertTrue(walkNode.has("performingPlayerId"));
        assertTrue(walkNode.has("destinationCardId"));
        assertTrue(walkNode.has("originCardId"));

        assertEquals("WALK_CARDS", walkNode.get("type").textValue());
        assertEquals(P1, walkNode.get("performingPlayerId").textValue());
        assertTrue(jsonListIsCardIds(walkNode.get("targetCardIds"), cardsToWalk));
        assertEquals(cardsToWalk.getFirst().getCardId(), walkNode.get("targetCardIds").get(0).asInt());
        assertEquals(outpost.getCardId(), walkNode.get("originCardId").intValue());
        assertEquals(runabout.getCardId(), walkNode.get("destinationCardId").intValue());
    }

}