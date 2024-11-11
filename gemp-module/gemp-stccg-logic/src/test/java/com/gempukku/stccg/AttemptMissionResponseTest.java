package com.gempukku.stccg;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.InvalidGameLogicException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class AttemptMissionResponseTest extends AbstractAtTest {

    @Test
    public void attemptMissionResponseTest() throws DecisionResultInvalidException, InvalidGameLogicException {
        initializeQuickMissionAttemptWithRisk();

        // Figure out which player is going first
        assertEquals(P1, _game.getCurrentPlayerId());

        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) {
            skipDilemma();
        }

        FacilityCard outpost = null;
        MissionCard excavation = null;
        PersonnelCard picard = null;
        ST1EPhysicalCard risk = null;
        PersonnelCard tarses = null;

        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Federation Outpost") && card instanceof FacilityCard facility)
                outpost = facility;
            if (Objects.equals(card.getTitle(), "Excavation") && card instanceof MissionCard mission)
                excavation = mission;
            if (Objects.equals(card.getTitle(), "Jean-Luc Picard") && card instanceof PersonnelCard personnel)
                picard = personnel;
            if (Objects.equals(card.getTitle(), "Risk is Our Business") && card instanceof ST1EPhysicalCard incident)
                risk = incident;
            if (Objects.equals(card.getTitle(), "Simon Tarses") && card instanceof PersonnelCard personnel)
                tarses = personnel;
        }

        assertNotNull(outpost);
        assertNotNull(excavation);
        assertNotNull(picard);
        assertNotNull(risk);
        assertNotNull(tarses);

        // Seed Risk is Our Business
        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        seedCard(P1, risk);
        assertTrue(risk.isInPlay());

        // Seed Federation Outpost at Excavation
        seedFacility(P1, outpost, excavation.getLocation());
        assertEquals(outpost.getLocation(), excavation.getLocation());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        // Report Picard to outpost
        reportCard(P1, picard, outpost);
        assertTrue(outpost.getCrew().contains(picard));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        // Beam Picard to the planet
        beamCard(P1, outpost, picard, excavation);
        assertTrue(picard.getAwayTeam().isOnSurface(excavation));

        // Attempt mission
        attemptMission(P1, picard.getAwayTeam(), excavation);

        // Respond by downloading Simon Tarses
        assertNotNull(_userFeedback.getAwaitingDecision(P1));
        playerDecided(P1,"0");
        assertTrue(_userFeedback.getAwaitingDecision(P1) instanceof ArbitraryCardsSelectionDecision);
        ((ArbitraryCardsSelectionDecision) (_userFeedback.getAwaitingDecision(P1)))
                .decisionMade(tarses);
        _game.getGameState().playerDecisionFinished(P1, _userFeedback);
        assertFalse(excavation.isCompleted());
        _game.carryOutPendingActionsUntilDecisionNeeded();
        assertTrue(outpost.getCrew().contains(tarses));

        // Confirm that mission was solved and player earned points
        assertTrue(excavation.isCompleted());
        assertEquals(excavation.getPoints(), _game.getGameState().getPlayerScore(P1));

        // Initiate a beam action from the outpost
        BeamCardsAction beamAction = selectAction(BeamCardsAction.class, outpost, P1);
        assertEquals(2, beamAction.getValidFromCards(_game).size());
        System.out.println(_userFeedback.getAwaitingDecision(P1).getClass().getSimpleName());
        selectCard(P1, outpost);
        assertEquals(picard.getAwayTeam(), tarses.getAwayTeam());

        List<Action> performedActions = _game.getActionsEnvironment().getPerformedActions();
        int performedId = 1;

        for (Action action : performedActions) {
            System.out.println(
                    performedId + " - " + action.getClass().getSimpleName() +
                            " (" + action.getActionType().name() + ")" +
                    ((action.getText(_game) != null) ? " - " + action.getText(_game) : ""));
            performedId++;
        }
    }

}