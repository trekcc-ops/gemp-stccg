package com.gempukku.stccg;

import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class MisSeedTest extends AbstractAtTest {

    @Test
    public void misSeedTest() throws DecisionResultInvalidException, InvalidGameLogicException {
        initializeQuickMissionAttempt();

        // Figure out which player is going first
        assertEquals(P1, _game.getCurrentPlayerId());

        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) {
            skipDilemma();
        }

        FacilityCard outpost = null;
        MissionCard excavation = null;
        PersonnelCard picard = null;
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

        // Seed Simon Tarses under Excavation
        _game.getGameState().seedCardsUnder(Collections.singleton(tarses), excavation);

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

        // Confirm that mission was not solved and Simon Tarses was removed from play
        assertEquals(Zone.REMOVED, tarses.getZone());
        assertFalse(excavation.isCompleted());
    }
}