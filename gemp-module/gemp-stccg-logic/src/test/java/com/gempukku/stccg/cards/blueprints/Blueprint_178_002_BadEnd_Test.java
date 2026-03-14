package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_178_002_BadEnd_Test extends AbstractAtTest {

    private PhysicalCard badEnd;
    private List<PersonnelCard> stoppedPersonnel;
    private List<PersonnelCard> initiallyUnstoppedPersonnel;
    private MissionCard mission;

    private void initializeGame(boolean overcomeDilemma) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        badEnd = builder.addSeedCardUnderMission("178_002", "A Bad End", P1, mission);
        stoppedPersonnel = new ArrayList<>();
        initiallyUnstoppedPersonnel = new ArrayList<>();

        // add 10 personnel; stop 5 of them
        for (int i = 0; i < 10; i++) {
            PersonnelCard personnel = (!overcomeDilemma) ?
                    builder.addCardOnPlanetSurface("101_242", "Taitt", P1, mission, PersonnelCard.class) :
                    builder.addCardOnPlanetSurface("163_032", "Martinez", P1, mission, PersonnelCard.class);
            if (i % 2 == 0) {
                builder.stopCard(personnel);
                stoppedPersonnel.add(personnel);
            } else {
                initiallyUnstoppedPersonnel.add(personnel);
            }
        }
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void failDilemmaTest() throws Exception {
        initializeGame(false);
        attemptMission(P1, mission);

        // All stopped personnel should be selectable
        assertTrue(selectableCardsAre(P1, stoppedPersonnel));
        assertEquals(5, stoppedPersonnel.size());

        // Must select 3 of 5 (half, rounded up)
        assertThrows(DecisionResultInvalidException.class, () -> selectCards(P1, stoppedPersonnel.subList(0,2)));
        assertThrows(DecisionResultInvalidException.class, () -> selectCards(P1, stoppedPersonnel.subList(0,4)));
        selectCards(P1, stoppedPersonnel.subList(0,3));

        for (PersonnelCard personnel : stoppedPersonnel.subList(0,3)) {
            assertTrue(personnelWasKilledAndDiscarded(personnel));
        }

        // Remaining Away Team is stopped because you don't have Biology x2
        for (PersonnelCard personnel : initiallyUnstoppedPersonnel) {
            assertTrue(personnel.isStopped());
        }

        // Dilemma is re-seeded
        assertTrue(badEnd.isSeededUnderMission(_game));
    }

    @Test
    public void passDilemmaTest() throws Exception {
        initializeGame(true);
        attemptMission(P1, mission);

        // All stopped personnel should be selectable
        assertTrue(selectableCardsAre(P1, stoppedPersonnel));
        assertEquals(5, stoppedPersonnel.size());

        // Must select 3 of 5 (half, rounded up)
        assertThrows(DecisionResultInvalidException.class, () -> selectCards(P1, stoppedPersonnel.subList(0,2)));
        assertThrows(DecisionResultInvalidException.class, () -> selectCards(P1, stoppedPersonnel.subList(0,4)));
        selectCards(P1, stoppedPersonnel.subList(0,3));

        for (PersonnelCard personnel : stoppedPersonnel.subList(0,3)) {
            assertTrue(personnelWasKilledAndDiscarded(personnel));
        }

        // Nobody else is stopped because you have Biology x2
        for (PersonnelCard personnel : initiallyUnstoppedPersonnel) {
            assertFalse(personnel.isStopped());
        }

        // Dilemma is removed from the game
        assertFalse(badEnd.isSeededUnderMission(_game));
        assertEquals(Zone.REMOVED, badEnd.getZone());
    }


}