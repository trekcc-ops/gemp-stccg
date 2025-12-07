package com.gempukku.stccg;

import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class AttemptMissionTest extends AbstractAtTest {

    @Test
    public void attemptMissionTest() throws DecisionResultInvalidException, InvalidGameLogicException, PlayerNotFoundException, InvalidGameOperationException {
        initializeGameToTestMissionAttempt();

        // Figure out which player is going first
        assertEquals(P1, _game.getCurrentPlayerId());

        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) {
            skipDilemma();
        }

        FacilityCard outpost = null;
        MissionCard excavation = null;
        PersonnelCard picard = null;

        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Federation Outpost") && card instanceof FacilityCard facility)
                outpost = facility;
            if (Objects.equals(card.getTitle(), "Excavation") && card instanceof MissionCard mission)
                excavation = mission;
            if (Objects.equals(card.getTitle(), "Jean-Luc Picard") && card instanceof PersonnelCard personnel)
                picard = personnel;
        }

        assertNotNull(outpost);
        assertNotNull(excavation);
        assertNotNull(picard);
        picard.removeFromCardGroup(_game);
        _game.getPlayer(P1).getDrawDeck().addCardToTop(picard);

        // Seed Federation Outpost at Excavation
        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
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

        // Confirm that mission was solved and player earned points
        assertTrue(excavation.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
        assertEquals(excavation.getPoints(), _game.getPlayer(P1).getScore());
    }

    @Test
    public void selectAwayTeamTest() throws DecisionResultInvalidException, InvalidGameLogicException, PlayerNotFoundException, InvalidGameOperationException {
        initializeGameToTestMissionAttempt();

        // Figure out which player is going first
        assertEquals(P1, _game.getCurrentPlayerId());

        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) {
            skipDilemma();
        }

        FacilityCard outpost = null;
        MissionCard excavation = null;
        PersonnelCard picard = null;

        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Federation Outpost") && card instanceof FacilityCard facility)
                outpost = facility;
            if (Objects.equals(card.getTitle(), "Excavation") && card instanceof MissionCard mission)
                excavation = mission;
            if (Objects.equals(card.getTitle(), "Jean-Luc Picard") && card instanceof PersonnelCard personnel)
                picard = personnel;
        }

        assertNotNull(outpost);
        assertNotNull(excavation);
        assertNotNull(picard);
        picard.removeFromCardGroup(_game);
        _game.getPlayer(P1).getDrawDeck().addCardToTop(picard);

        // Seed Federation Outpost at Excavation
        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());
        seedFacility(P1, outpost, excavation);
        assertEquals(outpost.getLocationDeprecatedOnlyUseForTests(_game), excavation.getLocationDeprecatedOnlyUseForTests(_game));
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        // Report Picard to outpost
        reportCard(P1, picard, outpost);
        assertTrue(outpost.hasCardInCrew(picard));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        MissionLocation excavationLocation = excavation.getLocationDeprecatedOnlyUseForTests(_game);

        // Beam Picard to the planet
        beamCard(P1, outpost, picard, excavation);
        assertTrue(_game.getGameState().getAwayTeamForCard(picard).isOnSurface(excavationLocation));

        // Attempt mission without specifying Away Team
        attemptMission(P1, excavationLocation);

        // Confirm that mission was solved and player earned points
        assertTrue(excavationLocation.isCompleted());
        assertEquals(excavation.getPoints(), _game.getPlayer(P1).getScore());
    }
}