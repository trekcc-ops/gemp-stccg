package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.turn.UseGameTextAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_109_063_AMS_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private PhysicalCard ams;
    private PhysicalCard tarses1;
    private PhysicalCard wallace1;
    private PhysicalCard tarses2;
    private PhysicalCard wallace2;
    private MissionCard mission;

    private void initializeGame(boolean amsAlreadyInPlay, boolean missionSpecialistsCanHelp) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        if (amsAlreadyInPlay) {
            ams = builder.addCardToCoreAsSeeded("109_063", "Assign Mission Specialists", P1);
            mission = builder.addMission("155_035", "Acquire Surplus Ships", P1);
                // Acquisition + ENGINEER + Computer Skill OR Diplomacy + Greed + Treachery
            builder.addCardOnPlanetSurface("155_088", "Q'elyn", P1, mission); // mission specialist Acquisition + ENGINEER
            if (missionSpecialistsCanHelp) {
                builder.addCardOnPlanetSurface("101_277", "Kurak", P1, mission); // Computer Skill
            } else {
                // Jo'Bril - Greed + Treachery
                // Captain Worf - Diplomacy
                builder.addCardOnPlanetSurface("101_299", "Jo'Bril", P1, mission);
                builder.addCardOnPlanetSurface("155_079", "Captain Worf", P1, mission);
            }
        } else {
            ams = builder.addSeedDeckCard("109_063", "Assign Mission Specialists", P1);
        }
        tarses1 = builder.addDrawDeckCard("101_236", "Simon Tarses", P1);
        wallace1 = builder.addDrawDeckCard("101_203", "Darian Wallace", P1);
        tarses2 = builder.addDrawDeckCard("101_236", "Simon Tarses", P2);
        wallace2 = builder.addDrawDeckCard("101_203", "Darian Wallace", P2);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1); // Federation Outpost
        if (amsAlreadyInPlay) {
            builder.setPhase(Phase.EXECUTE_ORDERS);
        } else {
            builder.setPhase(Phase.SEED_FACILITY);
        }
        builder.startGame();
    }

    @Test
    public void downloadSpecialistsAndDiscardTest()
            throws DecisionResultInvalidException, InvalidGameOperationException, CardNotFoundException {
        initializeGame(false, false);

        seedCard(P1, ams);
        assertTrue(ams.isInPlay());
        useGameText(P1, ams);
        assertNotNull(_game.getAwaitingDecision(P1));


        List<PersonnelCard> specialists = List.of((PersonnelCard) tarses1, (PersonnelCard) wallace1);
        assertTrue(getSelectableCards(P1).containsAll(specialists));
        assertFalse(getSelectableCards(P1).containsAll(List.of(tarses2, wallace2)));

        selectCards(P1, specialists);
        for (PersonnelCard specialist : specialists) {
            assertTrue(specialist.isInPlay());
            assertTrue(specialist.isAboard(outpost));
        }

        while (_game.getCurrentPhase() == Phase.SEED_FACILITY) {
            skipFacility();
        }

        // Try to discard card at start of turn
        assertEquals(Phase.START_OF_TURN, _game.getCurrentPhase());
        performAction(P1, UseGameTextAction.class, ams);

        assertEquals(Zone.DISCARD, ams.getZone());
        assertTrue(_game.getPlayer(P1).getDiscardPile().contains(ams));
    }

    @Test
    public void bonusPointsTest() throws Exception {
        initializeGame(true, true);
        assertEquals(0, _game.getPlayer(P1).getScore());
        AttemptMissionAction attemptAction = attemptMission(P1, mission);
        assertTrue(attemptAction.wasSuccessful());
        assertEquals(30, _game.getPlayer(P1).getScore());
        useGameText(P1, ams);
        assertEquals(35, _game.getPlayer(P1).getScore());
    }

    @Test
    public void cannotScorePointsTest() throws Exception {
        initializeGame(true, false);
        assertEquals(0, _game.getPlayer(P1).getScore());
        AttemptMissionAction attemptAction = attemptMission(P1, mission);
        assertTrue(attemptAction.wasSuccessful());
        assertEquals(30, _game.getPlayer(P1).getScore());
        assertThrows(DecisionResultInvalidException.class, () -> useGameText(P1, ams));
    }

}