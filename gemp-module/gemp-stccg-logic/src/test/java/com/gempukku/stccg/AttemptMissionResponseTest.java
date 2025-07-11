package com.gempukku.stccg;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class AttemptMissionResponseTest extends AbstractAtTest {

    @Test
    public void attemptMissionResponseTest() throws DecisionResultInvalidException, InvalidGameLogicException, PlayerNotFoundException, InvalidGameOperationException, CardNotFoundException {
        initializeQuickMissionAttemptWithRisk();
        Player player1 = _game.getPlayer(P1);

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
        PersonnelCard tarses = (PersonnelCard) newCardForGame("101_236", P1);

        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Federation Outpost") && card instanceof FacilityCard facility)
                outpost = facility;
            if (Objects.equals(card.getTitle(), "Excavation") && card instanceof MissionCard mission)
                excavation = mission;
            if (Objects.equals(card.getTitle(), "Jean-Luc Picard") && card instanceof PersonnelCard personnel)
                picard = personnel;
            if (Objects.equals(card.getTitle(), "Risk is Our Business") && card instanceof ST1EPhysicalCard incident)
                risk = incident;
        }

        assertNotNull(outpost);
        assertNotNull(excavation);
        assertNotNull(picard);
        assertNotNull(risk);
        assertNotNull(tarses);
        picard.removeFromCardGroup();
        player1.getDrawDeck().addCardToTop(picard);
        player1.getDrawDeck().addCardToBottom(tarses);
        tarses.setZone(Zone.DRAW_DECK);

        // Seed Risk is Our Business
        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        seedCard(P1, risk);
        assertTrue(risk.isInPlay());

        // Seed Federation Outpost at Excavation
        seedFacility(P1, outpost, excavation.getLocationDeprecatedOnlyUseForTests());
        assertEquals(outpost.getLocationDeprecatedOnlyUseForTests(), excavation.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        // Report Picard to outpost
        reportCard(P1, picard, outpost);
        assertTrue(outpost.getCrew().contains(picard));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        // Beam Picard to the planet
        beamCard(P1, outpost, picard, excavation);
        assertTrue(picard.getAwayTeam().isOnSurface(excavation.getLocationDeprecatedOnlyUseForTests()));

        // Attempt mission
        attemptMission(P1, picard.getAwayTeam(), excavation);

        // Respond by downloading Simon Tarses
        assertNotNull(_userFeedback.getAwaitingDecision(P1));
        selectFirstAction(P1);
        assertInstanceOf(ArbitraryCardsSelectionDecision.class, _userFeedback.getAwaitingDecision(P1));
        ((ArbitraryCardsSelectionDecision) (_userFeedback.getAwaitingDecision(P1)))
                .decisionMade(tarses);
        _game.getGameState().playerDecisionFinished(P1, _userFeedback);
        assertFalse(excavation.getLocationDeprecatedOnlyUseForTests().isCompleted());
        _game.carryOutPendingActionsUntilDecisionNeeded();
        assertTrue(outpost.getCrew().contains(tarses));

        // Confirm that mission was solved and player earned points
        assertTrue(excavation.getLocationDeprecatedOnlyUseForTests().isCompleted());
        assertEquals(excavation.getPoints(), player1.getScore());

        // Initiate a beam action from the outpost using all the decisions involved
        BeamCardsAction beamAction = selectAction(BeamCardsAction.class, outpost, P1);
        assertEquals(2, beamAction.getValidFromCards(_game).size());
        selectCard(P1, outpost);
        assertEquals(picard.getAwayTeam(), tarses.getAwayTeam());
    }

}