package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_109_010_Maglock_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Maglock
    private FacilityCard outpost;
    private PersonnelCard picard;
    private MissionCard _mission;
    private PhysicalCard _maglock;
    private PersonnelCard troi;
    private PersonnelCard hobson;
    private PersonnelCard data;
    private ShipCard runabout;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        _maglock = builder.addSeedCard("109_010", "Maglock", P2, _mission);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        runabout = builder.addCardInHand("101_331", "Runabout", P1, ShipCard.class);
        troi = builder.addCardInHand("101_205", "Deanna Troi", P1, PersonnelCard.class);
        hobson = builder.addCardInHand("101_202", "Christopher Hobson", P1, PersonnelCard.class);
        picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class);
        data = builder.addCardInHand("101_204", "Data", P1, PersonnelCard.class);

        reportCardsToFacility(outpost, troi, hobson, picard, data, runabout);

        assertTrue(outpost.hasCardInCrew(troi));
        assertTrue(outpost.hasCardInCrew(hobson));
        assertTrue(outpost.hasCardInCrew(picard));
        assertTrue(outpost.hasCardInCrew(data));
        assertFalse(outpost.hasCardInCrew(runabout));
        assertEquals(outpost, runabout.getDockedAtCard(_game));

        _game.startGame();
    }


    @Test
    public void maglockFailedTest() throws DecisionResultInvalidException,
            CardNotFoundException, JsonProcessingException, InvalidGameOperationException {

        initializeGame();

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);
        personnelBeaming.add(hobson);
        personnelBeaming.add(picard);

        beamCards(P1, outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.hasCardInCrew(card));
            assertFalse(outpost.hasCardInCrew(card));
        }
        assertEquals(0, _game.getGameState().getAwayTeams().size());

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        for (PersonnelCard personnel : runabout.getAttemptingPersonnel(_game)) {
            assertTrue(personnel.isStopped());
        }
        assertTrue(runabout.isStopped());
        assertFalse(_mission.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
        assertTrue(_mission.getLocationDeprecatedOnlyUseForTests(_game).getSeedCards().contains(_maglock));
        String gameStateString = _game.getGameState().serializeComplete();
    }

    @Test
    public void maglockPassedTest() throws DecisionResultInvalidException, InvalidGameLogicException,
            CardNotFoundException, InvalidGameOperationException, PlayerNotFoundException {

        initializeGame();

        List<PersonnelCard> personnelBeaming = List.of(data, hobson, picard);

        beamCards(P1, outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.hasCardInCrew(card));
            assertFalse(outpost.hasCardInCrew(card));
        }
        assertEquals(0, _game.getGameState().getAwayTeams().size());

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        for (PersonnelCard personnel : runabout.getAttemptingPersonnel(_game)) {
            assertFalse(personnel.isStopped());
        }
        assertFalse(runabout.isStopped());
        assertTrue(_mission.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
        assertFalse(_mission.getLocationDeprecatedOnlyUseForTests(_game).getSeedCards().contains(_maglock));
        assertEquals(Zone.REMOVED, _maglock.getZone());
    }

}