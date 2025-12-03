package com.gempukku.stccg.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ActionSerializerTest extends AbstractAtTest {

       @Test
    public void killAttemptSerializerTest() throws CardNotFoundException, DecisionResultInvalidException,
            JsonProcessingException, PlayerNotFoundException, InvalidGameOperationException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        KillSinglePersonnelAction action = new KillSinglePersonnelAction(_game, P1, _game.getCardFromCardId(1),
                new SelectCardsFromDialogAction(_game, _game.getPlayer(P1), "Select a card", Filters.any));
        KillSinglePersonnelAction action2 = new KillSinglePersonnelAction(_game, P1, _game.getCardFromCardId(1),
                new SelectCardsFromDialogAction(_game, _game.getPlayer(P1), "Select a card", Filters.any));
        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", _cardLibrary, P1);
        PhysicalShipCard runabout =
                (PhysicalShipCard) _game.addCardToGame("101_331", _cardLibrary, P1);
        action.appendCost(action2);
        String jsonString = _game.getGameState().serializeForPlayer(P1);
        System.out.println(jsonString);
    }

//    @Test
    public void missionAttemptSerializerTest() throws Exception {
        initializeMissionAttemptWithDrawCards("Investigate Rogue Comet", "172_040"); // with M'Vil
        assertNotNull(_mission);

        ST1EPhysicalCard maglock =
                (ST1EPhysicalCard) _game.addCardToGame("109_010", _cardLibrary, P1);
        maglock.setZone(Zone.VOID);

        // Seed Maglock
        MissionLocation missionLocation = _mission.getLocationDeprecatedOnlyUseForTests();
        seedCardsUnder(Collections.singleton(maglock), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(), _mission.getLocationDeprecatedOnlyUseForTests());

        MissionLocation gault = null;
        for (MissionLocation location : _game.getGameState().getSpacelineLocations()) {
            if (location.getLocationName().equals("Gault")) {
                gault = location;
            }
        }
        assertNotNull(gault);

        // Seed Klingon Outpost
        seedFacility(P1, _klingonOutpost, gault);
        assertEquals(gault, _klingonOutpost.getLocationDeprecatedOnlyUseForTests());

        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", _cardLibrary, P1);
        PersonnelCard hobson = (PersonnelCard) _game.addCardToGame("101_202", _cardLibrary, P1);
        PersonnelCard picard = (PersonnelCard) _game.addCardToGame("101_215", _cardLibrary, P1);
        PersonnelCard data = (PersonnelCard) _game.addCardToGame("101_204", _cardLibrary, P1);
        PhysicalShipCard runabout =
                (PhysicalShipCard) _game.addCardToGame("101_331", _cardLibrary, P1);

        PersonnelCard mvil = null;
        for (PhysicalCard card : _game.getPlayer(P1).getCardsInHand()) {
            if (card.getBlueprintId().equals("172_040")) {
                mvil = (PersonnelCard) card;
            }
        }
        assertNotNull(mvil);
        troi.reportToFacility(_outpost);
        hobson.reportToFacility(_outpost);
        picard.reportToFacility(_outpost);
        data.reportToFacility(_outpost);
        runabout.reportToFacility(_outpost);
        reportCard(P1, mvil, _klingonOutpost);
        playerDecided(P1, "0"); // pick affiliation

        assertTrue(_outpost.getCrew().contains(troi));
        assertTrue(_outpost.getCrew().contains(hobson));
        assertTrue(_outpost.getCrew().contains(picard));
        assertTrue(_outpost.getCrew().contains(data));
        assertTrue(_klingonOutpost.getCrew().contains(mvil));
        assertFalse(_outpost.getCrew().contains(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);
        personnelBeaming.add(hobson);
        personnelBeaming.add(picard);

        beamCards(P1, _outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.getCrew().contains(card));
            assertFalse(_outpost.getCrew().contains(card));
        }

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        for (PersonnelCard personnel : runabout.getAttemptingPersonnel()) {
            assertTrue(personnel.isStopped());
        }
        assertTrue(runabout.isStopped());
        assertFalse(_mission.getLocationDeprecatedOnlyUseForTests().isCompleted());
        assertTrue(_mission.getLocationDeprecatedOnlyUseForTests().getSeedCards().contains(maglock));
        String gameStateString = _game.getGameState().serializeComplete();
    }

}