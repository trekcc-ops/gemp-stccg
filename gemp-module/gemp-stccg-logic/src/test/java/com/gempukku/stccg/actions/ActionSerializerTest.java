package com.gempukku.stccg.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.missionattempt.RevealSeedCardAction;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.actions.turn.PlayOutOptionalAfterResponsesAction;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.ST1EGameStateDeserializer;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ActionSerializerTest extends AbstractAtTest {

    @Test
    public void missionAttemptSerializerTest() throws Exception {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        assertNotNull(_mission);

        ST1EPhysicalCard maglock =
                (ST1EPhysicalCard) _game.getGameState().addCardToGame("109_010", _cardLibrary, P1);
        maglock.setZone(Zone.VOID);

        // Seed Maglock
        _game.getGameState().seedCardsUnder(Collections.singleton(maglock), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocation());
        assertEquals(_outpost.getLocation(), _mission.getLocation());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.getGameState().addCardToGame("101_205", _cardLibrary, P1);
        PersonnelCard hobson = (PersonnelCard) _game.getGameState().addCardToGame("101_202", _cardLibrary, P1);
        PersonnelCard picard = (PersonnelCard) _game.getGameState().addCardToGame("101_215", _cardLibrary, P1);
        PersonnelCard data = (PersonnelCard) _game.getGameState().addCardToGame("101_204", _cardLibrary, P1);
        PhysicalShipCard runabout =
                (PhysicalShipCard) _game.getGameState().addCardToGame("101_331", _cardLibrary, P1);

        troi.reportToFacility(_outpost);
        hobson.reportToFacility(_outpost);
        picard.reportToFacility(_outpost);
        data.reportToFacility(_outpost);
        runabout.reportToFacility(_outpost);

        assertTrue(_outpost.getCrew().contains(troi));
        assertTrue(_outpost.getCrew().contains(hobson));
        assertTrue(_outpost.getCrew().contains(picard));
        assertTrue(_outpost.getCrew().contains(data));
        assertFalse(_outpost.getCrew().contains(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard());
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

        int maxActionId = _game.getActionsEnvironment().getNextActionId() - 1;
        for (int i = 1; i <= maxActionId; i++) {
            Action action = _game.getActionsEnvironment().getActionById(i);
            String message = i + " [" + action.getActionId() + "] - " + action.getClass().getSimpleName() +
                    " (" + action.getActionType().name() + ")";
            String actionType = action.getClass().getSimpleName();
            if (!actionType.equals("PlayOutOptionalAfterResponsesAction") && !actionType.equals("PlayOutEffectResults")) {
                if (action.getActionSelectionText(_game) != null)
                    message = message + " - " + action.getActionSelectionText(_game);
                if (action instanceof SubAction)
                    message = message + " (SubAction)";
                if (action instanceof PlayOutOptionalAfterResponsesAction response)
                    message = message + " [ EffectResult = " + response.getEffectResults();
                System.out.println(message);
                String serialized = new ObjectMapper().writeValueAsString(action);
                System.out.println(serialized);
            } else {
                System.out.println(message);
            }
        };

        System.out.println();
        System.out.println();
        for (Action action : _game.getActionsEnvironment().getActionStack()) {
            String message = action.getActionId() + " - " + action.getClass().getSimpleName() +
                    " (" + action.getActionType().name() + ")";
            String actionType = action.getClass().getSimpleName();
            if (!actionType.equals("PlayOutOptionalAfterResponsesAction") && !actionType.equals("PlayOutEffectResults")) {
                if (action.getActionSelectionText(_game) != null)
                    message = message + " - " + action.getActionSelectionText(_game);
                if (action instanceof SubAction)
                    message = message + " (SubAction)";
                if (action instanceof PlayOutOptionalAfterResponsesAction response)
                    message = message + " [ EffectResult = " + response.getEffectResults();
                System.out.println(message);
                if (action instanceof RevealSeedCardAction revealAction) {
                    String serialized = new ObjectMapper().writeValueAsString(revealAction);
                    System.out.println(serialized);
                }
            } else {
                System.out.println(message);
            }
        }

/*        ObjectMapper mapper = new ObjectMapper();
        String serialized4 = mapper.writeValueAsString(_game.getGameState());
        System.out.println(serialized4.replace(",",",\n"));*/
    }

}