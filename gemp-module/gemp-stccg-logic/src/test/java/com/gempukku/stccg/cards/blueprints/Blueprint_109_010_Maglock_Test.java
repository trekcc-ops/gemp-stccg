package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_109_010_Maglock_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Maglock

    @Test
    public void maglockFailedTest() throws DecisionResultInvalidException, InvalidGameLogicException,
            CardNotFoundException, JsonProcessingException, InvalidGameOperationException, PlayerNotFoundException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        assertNotNull(_mission);

        ST1EPhysicalCard maglock =
                (ST1EPhysicalCard) _game.addCardToGame("109_010", _cardLibrary, P1);
        maglock.setZone(Zone.VOID);

        // Seed Maglock
        MissionLocation missionLocation = _mission.getLocationDeprecatedOnlyUseForTests();
        seedCardsUnder(Collections.singleton(maglock), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(), _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", _cardLibrary, P1);
        PersonnelCard hobson = (PersonnelCard) _game.addCardToGame("101_202", _cardLibrary, P1);
        PersonnelCard picard = (PersonnelCard) _game.addCardToGame("101_215", _cardLibrary, P1);
        PersonnelCard data = (PersonnelCard) _game.addCardToGame("101_204", _cardLibrary, P1);
        ShipCard runabout =
                (ShipCard) _game.addCardToGame("101_331", _cardLibrary, P1);

        reportCardsToFacility(_outpost, troi, hobson, picard, data, runabout);

        assertTrue(_outpost.hasCardInCrew(troi));
        assertTrue(_outpost.hasCardInCrew(hobson));
        assertTrue(_outpost.hasCardInCrew(picard));
        assertTrue(_outpost.hasCardInCrew(data));
        assertFalse(_outpost.hasCardInCrew(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);
        personnelBeaming.add(hobson);
        personnelBeaming.add(picard);

        beamCards(P1, _outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.hasCardInCrew(card));
            assertFalse(_outpost.hasCardInCrew(card));
        }
        assertEquals(0, _game.getGameState().getAwayTeams().size());

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        for (PersonnelCard personnel : runabout.getAttemptingPersonnel(_game)) {
            assertTrue(personnel.isStopped());
        }
        assertTrue(runabout.isStopped());
        assertFalse(_mission.getLocationDeprecatedOnlyUseForTests().isCompleted());
        assertTrue(_mission.getLocationDeprecatedOnlyUseForTests().getSeedCards().contains(maglock));
        String gameStateString = _game.getGameState().serializeComplete();
    }

    @Test
    public void maglockPassedTest() throws DecisionResultInvalidException, InvalidGameLogicException,
            CardNotFoundException, InvalidGameOperationException, PlayerNotFoundException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");

        ST1EPhysicalCard maglock =
                (ST1EPhysicalCard) _game.addCardToGame("109_010", _cardLibrary, P1);
        maglock.setZone(Zone.VOID);

        // Seed Maglock
        MissionLocation missionLocation = _mission.getLocationDeprecatedOnlyUseForTests();
        seedCardsUnder(Collections.singleton(maglock), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(), _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", _cardLibrary, P1);
        PersonnelCard hobson = (PersonnelCard) _game.addCardToGame("101_202", _cardLibrary, P1);
        PersonnelCard picard = (PersonnelCard) _game.addCardToGame("101_215", _cardLibrary, P1);
        PersonnelCard data = (PersonnelCard) _game.addCardToGame("101_204", _cardLibrary, P1);
        ShipCard runabout =
                (ShipCard) _game.addCardToGame("101_331", _cardLibrary, P1);

        reportCardsToFacility(_outpost, troi, hobson, picard, data, runabout);

        assertTrue(_outpost.hasCardInCrew(troi));
        assertTrue(_outpost.hasCardInCrew(hobson));
        assertTrue(_outpost.hasCardInCrew(picard));
        assertTrue(_outpost.hasCardInCrew(data));
        assertFalse(_outpost.hasCardInCrew(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(data);
        personnelBeaming.add(hobson);
        personnelBeaming.add(picard);

        beamCards(P1, _outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.hasCardInCrew(card));
            assertFalse(_outpost.hasCardInCrew(card));
        }
        assertEquals(0, _game.getGameState().getAwayTeams().size());

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        for (PersonnelCard personnel : runabout.getAttemptingPersonnel(_game)) {
            assertFalse(personnel.isStopped());
        }
        assertFalse(runabout.isStopped());
        assertTrue(_mission.getLocationDeprecatedOnlyUseForTests().isCompleted());
        assertFalse(_mission.getLocationDeprecatedOnlyUseForTests().getSeedCards().contains(maglock));
        assertEquals(Zone.REMOVED, maglock.getZone());
    }

}