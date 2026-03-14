package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RandomSelectionResultTest extends AbstractAtTest implements ActionResultTest {

    private MissionCard mission;
    private PhysicalCard armus;
    private PersonnelCard harry;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        armus = builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, mission);
        harry = builder.addCardOnPlanetSurface("123_120", "Harry Kim", P1, mission, PersonnelCard.class);
        builder.addCardOnPlanetSurface("101_251", "Worf", P1, mission, PersonnelCard.class);

        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void serializeResultForYouTest() throws Exception {
        initializeGame();
        attemptMission(P1, mission);
        useGameText(P1, harry);

        JsonNode json = _game.serializeGameStateForPlayer(P1);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode randomSelectionNode = null;
        JsonNode volunteerNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("RANDOM_SELECTION_INITIATED")) {
                randomSelectionNode = resultsNode.get(i);
                volunteerNode = resultsNode.get(i+1);
                assertEquals("VOLUNTEERED_FOR_SELECTION", volunteerNode.get("type").textValue());
                break;
            }
        }

        assertNotNull(randomSelectionNode);
        assertNotNull(volunteerNode);

        assertSerializedFields(randomSelectionNode);
        assertSerializedFields(volunteerNode, "volunteeringCardId", "selectingCardId");

        assertEquals(harry.getCardId(), volunteerNode.get("volunteeringCardId").intValue());
        assertEquals(armus.getCardId(), volunteerNode.get("selectingCardId").intValue());
    }

    @Test
    public void serializeResultForOpponentTest() throws Exception {
        initializeGame();
        attemptMission(P1, mission);
        useGameText(P1, harry);

        JsonNode json = _game.serializeGameStateForPlayer(P2);
        JsonNode resultsNode = json.get("actionResults");

        JsonNode randomSelectionNode = null;
        JsonNode volunteerNode = null;

        for (int i = 0; i < resultsNode.size(); i++) {
            if (resultsNode.get(i).get("type").textValue().equals("RANDOM_SELECTION_INITIATED")) {
                randomSelectionNode = resultsNode.get(i);
                volunteerNode = resultsNode.get(i+1);
                assertEquals("VOLUNTEERED_FOR_SELECTION", volunteerNode.get("type").textValue());
                break;
            }
        }

        assertNotNull(randomSelectionNode);
        assertNotNull(volunteerNode);

        assertSerializedFields(randomSelectionNode);
        assertSerializedFields(volunteerNode, "volunteeringCardId", "selectingCardId");

        assertEquals(harry.getCardId(), volunteerNode.get("volunteeringCardId").intValue());
        assertEquals(armus.getCardId(), volunteerNode.get("selectingCardId").intValue());
    }

}