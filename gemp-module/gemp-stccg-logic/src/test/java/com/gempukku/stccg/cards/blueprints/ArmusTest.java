package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ArmusTest extends AbstractAtTest {

    @Test
    public void armusTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameLogicException, InvalidGameOperationException, PlayerNotFoundException {
        initializeGameToTestMissionAttempt();

        // Figure out which player is going first
        assertEquals(P1, _game.getCurrentPlayerId());

        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) {
            skipDilemma();
        }
        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());

        FacilityCard outpost = null;
        MissionCard excavation = null;
        PersonnelCard picard = (PersonnelCard) newCardForGame("135_004", P1);
        PersonnelCard tarses = null;

        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Federation Outpost") && card instanceof FacilityCard facility)
                outpost = facility;
            if (Objects.equals(card.getTitle(), "Excavation") && card instanceof MissionCard mission)
                excavation = mission;
            if (Objects.equals(card.getTitle(), "Jean-Luc Picard") && card instanceof PersonnelCard personnel)
                picard = personnel;
            if (Objects.equals(card.getTitle(), "Simon Tarses") && card instanceof PersonnelCard personnel)
                tarses = personnel;
        }

        assertNotNull(outpost);
        assertNotNull(excavation);
        assertNotNull(picard);
        assertNotNull(tarses);
        picard.removeFromCardGroup(_game);
        _game.getPlayer(P1).getDrawDeck().addCardToTop(picard);

        PhysicalCard armus = _game.addCardToGame("101_015", _cardLibrary, P2);
        armus.setZone(Zone.VOID);

        // Seed Armus under Excavation
        MissionLocation kurl = excavation.getLocationDeprecatedOnlyUseForTests();
        seedCardsUnder(Collections.singleton(armus), excavation);

        // Seed Federation Outpost at Excavation
        seedFacility(P1, outpost, excavation);
        assertEquals(outpost.getLocationDeprecatedOnlyUseForTests(), excavation.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        // Report Picard to outpost
        reportCard(P1, picard, outpost);
        assertTrue(outpost.hasCardInCrew(picard));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        // Beam Picard to the planet
        beamCard(P1, outpost, picard, excavation);
        assertTrue(picard.getAwayTeam().isOnSurface(excavation.getLocationDeprecatedOnlyUseForTests()));

        // Attempt mission
        attemptMission(P1, picard.getAwayTeam(), excavation);

        // Confirm that Picard was discarded
        assertEquals(Zone.DISCARD, picard.getZone());

        // Confirm that Armus was removed from the game
        assertEquals(Zone.REMOVED, armus.getZone());

        // Confirm the game has no Away Teams remaining
        assertEquals(0, _game.getGameState().getAwayTeams().size());

        // Confirm that mission was not solved
        assertFalse(excavation.getLocationDeprecatedOnlyUseForTests().isCompleted());

        // Confirm the mission attempt was added to performed actions
        int missionAttempts = 0;
        for (Action action : _game.getActionsEnvironment().getPerformedActions())
            if (action instanceof AttemptMissionAction missionAction && missionAction.wasFailed())
                missionAttempts++;
        assertEquals(1, missionAttempts);
    }
}