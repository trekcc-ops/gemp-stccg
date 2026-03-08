package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BeamCardsActionResultTest extends AbstractAtTest {

    private MissionCard mission;
    private List<PersonnelCard> cardsToBeam;
    private FacilityCard outpost;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P2);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1, mission);

        cardsToBeam = new ArrayList<>();
        cardsToBeam.add(builder.addCardAboardShipOrFacility("101_242", "Taitt", P1, outpost, PersonnelCard.class));
        cardsToBeam.add(builder.addCardAboardShipOrFacility("101_242", "Taitt", P1, outpost, PersonnelCard.class));
        cardsToBeam.add(builder.addCardAboardShipOrFacility("101_242", "Taitt", P1, outpost, PersonnelCard.class));
        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();
        Action beamAction = beamCards(P1, outpost, cardsToBeam, mission);

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");
        JsonNode beamNode = resultsNode.get(resultsNode.size() - 1);
        assertEquals(7, beamNode.size());

        assertTrue(beamNode.has("timestamp"));
        assertTrue(beamNode.has("targetCardIds"));
        assertTrue(beamNode.has("resultId"));
        assertTrue(beamNode.has("type"));
        assertTrue(beamNode.has("performingPlayerId"));
        assertTrue(beamNode.has("destinationCardId"));
        assertTrue(beamNode.has("originCardId"));

        assertEquals("BEAM_CARDS", beamNode.get("type").textValue());
        assertEquals(P1, beamNode.get("performingPlayerId").textValue());
        assertEquals(cardsToBeam.size(), beamNode.get("targetCardIds").size());
        assertEquals(cardsToBeam.getFirst().getCardId(), beamNode.get("targetCardIds").get(0).asInt());
        assertEquals(outpost.getCardId(), beamNode.get("originCardId").intValue());
        assertEquals(mission.getCardId(), beamNode.get("destinationCardId").intValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        Action beamAction = beamCards(P1, outpost, cardsToBeam, mission);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");
        JsonNode beamNode = resultsNode.get(resultsNode.size() - 1);
        assertEquals(7, beamNode.size());

        assertTrue(beamNode.has("timestamp"));
        assertTrue(beamNode.has("targetCardIds"));
        assertTrue(beamNode.has("resultId"));
        assertTrue(beamNode.has("type"));
        assertTrue(beamNode.has("performingPlayerId"));
        assertTrue(beamNode.has("destinationCardId"));
        assertTrue(beamNode.has("originCardId"));

        assertEquals("BEAM_CARDS", beamNode.get("type").textValue());
        assertEquals(P1, beamNode.get("performingPlayerId").textValue());
        assertEquals(cardsToBeam.size(), beamNode.get("targetCardIds").size());
        assertEquals(cardsToBeam.getFirst().getCardId(), beamNode.get("targetCardIds").get(0).asInt());
        assertEquals(outpost.getCardId(), beamNode.get("originCardId").intValue());
        assertEquals(mission.getCardId(), beamNode.get("destinationCardId").intValue());
    }

}