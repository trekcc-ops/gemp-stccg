package com.gempukku.stccg.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.actions.missionattempt.RevealSeedCardAction;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.actions.turn.PlayOutOptionalResponsesAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ActionSerializerTest extends AbstractAtTest {

//    @Test
    public void killAttemptSerializerTest() throws CardNotFoundException, DecisionResultInvalidException, JsonProcessingException, InvalidGameLogicException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        KillSinglePersonnelAction action = new KillSinglePersonnelAction(_game.getPlayer(P1), _game.getCardFromCardId(1),
                new SelectCardsFromDialogAction(_game.getPlayer(P1), "Select a card", Filters.any));
        KillSinglePersonnelAction action2 = new KillSinglePersonnelAction(_game.getPlayer(P1), _game.getCardFromCardId(1),
                new SelectCardsFromDialogAction(_game.getPlayer(P1), "Select a card", Filters.any));
        PersonnelCard troi = (PersonnelCard) _game.getGameState().addCardToGame("101_205", _cardLibrary, P1);
        PhysicalShipCard runabout =
                (PhysicalShipCard) _game.getGameState().addCardToGame("101_331", _cardLibrary, P1);
        action.appendCost(action2);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(action);
        System.out.println(jsonString);
        System.out.println(mapper.writeValueAsString(_mission));
        System.out.println(mapper.writeValueAsString(_mission.getLocation()));
        System.out.println(mapper.writeValueAsString(_game.getCardFromCardId(1)));
        System.out.println(mapper.writeValueAsString(troi));
        System.out.println(mapper.writeValueAsString(runabout));
        System.out.println(mapper.writeValueAsString(_game.getGameState()));
    }

    @Test
    public void missionAttemptSerializerTest() throws Exception {
        initializeMissionAttemptWithDrawCards("Investigate Rogue Comet", "172_040"); // with M'Vil
        assertNotNull(_mission);

        ST1EPhysicalCard maglock =
                (ST1EPhysicalCard) _game.getGameState().addCardToGame("109_010", _cardLibrary, P1);
        maglock.setZone(Zone.VOID);

        // Seed Maglock
        _game.getGameState().seedCardsUnder(Collections.singleton(maglock), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocation());
        assertEquals(_outpost.getLocation(), _mission.getLocation());

        MissionLocation gault = null;
        for (MissionLocation location : _game.getGameState().getSpacelineLocations()) {
            if (location.getLocationName().equals("Gault")) {
                gault = location;
            }
        }
        assertNotNull(gault);

        // Seed Klingon Outpost
        seedFacility(P1, _klingonOutpost, gault);
        assertEquals(gault, _klingonOutpost.getLocation());

        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.getGameState().addCardToGame("101_205", _cardLibrary, P1);
        PersonnelCard hobson = (PersonnelCard) _game.getGameState().addCardToGame("101_202", _cardLibrary, P1);
        PersonnelCard picard = (PersonnelCard) _game.getGameState().addCardToGame("101_215", _cardLibrary, P1);
        PersonnelCard data = (PersonnelCard) _game.getGameState().addCardToGame("101_204", _cardLibrary, P1);
        PhysicalShipCard runabout =
                (PhysicalShipCard) _game.getGameState().addCardToGame("101_331", _cardLibrary, P1);

        PersonnelCard mvil = null;
        for (PhysicalCard card : _game.getGameState().getHand(P1)) {
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
        assertFalse(_mission.getLocation().isCompleted());
        assertTrue(_mission.getLocation().getCardsSeededUnderneath().contains(maglock));
        showSerializedActions();
    }

}