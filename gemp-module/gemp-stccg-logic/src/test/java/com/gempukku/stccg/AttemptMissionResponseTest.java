package com.gempukku.stccg;

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

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class AttemptMissionResponseTest extends AbstractAtTest {


    // TODO - Come back to this one. Risk is Our Business has an incomplete definition right now.
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
        picard.removeFromCardGroup(_game);
        player1.getDrawDeck().addCardToTop(picard);
        player1.getDrawDeck().addCardToBottom(tarses);
        tarses.setZone(Zone.DRAW_DECK);

        // Seed Risk is Our Business
        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        seedCard(P1, risk);
        assertTrue(risk.isInPlay());

        // Seed Federation Outpost at Excavation
        seedFacility(P1, outpost, excavation);
        assertEquals(outpost.getLocationDeprecatedOnlyUseForTests(_game), excavation.getLocationDeprecatedOnlyUseForTests(_game));
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        // Report Picard to outpost
        reportCard(P1, picard, outpost);
        assertTrue(outpost.hasCardInCrew(picard));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        // Beam Picard to the planet
        beamCard(P1, outpost, picard, excavation);
        assertTrue(_game.getGameState().getAwayTeamForCard(picard).isOnSurface(excavation.getLocationDeprecatedOnlyUseForTests(_game)));

        // Attempt mission
        attemptMission(P1, _game.getGameState().getAwayTeamForCard(picard), excavation);

        // Respond by downloading Simon Tarses
        assertNotNull(_game.getAwaitingDecision(P1));
        selectFirstAction(P1);
        assertInstanceOf(ArbitraryCardsSelectionDecision.class, _game.getAwaitingDecision(P1));
        ((ArbitraryCardsSelectionDecision) (_game.getAwaitingDecision(P1)))
                .decisionMade(tarses);
        _game.removeDecision(P1);
        assertFalse(excavation.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
        _game.carryOutPendingActionsUntilDecisionNeeded();
        assertTrue(outpost.hasCardInCrew(tarses));

        // Confirm that mission was solved and player earned points
        assertTrue(excavation.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
        assertEquals(excavation.getPoints(), player1.getScore());

        // Initiate a beam action from the outpost using all the decisions involved
        BeamCardsAction beamAction = selectAction(BeamCardsAction.class, outpost, P1);
        assertEquals(2, beamAction.getValidFromCards(_game).size());
        selectCard(P1, outpost);
        assertEquals(_game.getGameState().getAwayTeamForCard(picard), _game.getGameState().getAwayTeamForCard(tarses));
    }

}