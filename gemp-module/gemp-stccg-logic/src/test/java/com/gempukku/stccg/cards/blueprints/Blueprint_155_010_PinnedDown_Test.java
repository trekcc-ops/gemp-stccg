package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectRandomCardsAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_155_010_PinnedDown_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard pinnedDown;
    private Collection<PersonnelCard> attemptingPersonnel;
    private PhysicalCard qCard;

    private void initializeGame(int personnelToAttempt, boolean includeQCard)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        if (includeQCard) {
            qCard = builder.addCardToCoreAsSeeded("991_006", "Dummy 1E Q Card", P1);
        }
        pinnedDown = builder.addSeedCardUnderMission("155_010", "Pinned Down", P2, _mission);
        ShipCard runabout = builder.addShipInSpace("101_331", "Runabout", P1, _mission);
        attemptingPersonnel = new ArrayList<>();
        for (int i = 0; i < personnelToAttempt; i++) {
            PersonnelCard larson = builder.addCardAboardShipOrFacility(
                    "101_220", "Linda Larson", P1, runabout, PersonnelCard.class);
            attemptingPersonnel.add(larson);
        }
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    private void initializeGame(int personnelToAttempt) throws Exception {
        initializeGame(personnelToAttempt, false);
    }

    @Test
    public void failDilemmaTest() throws Exception {
        initializeGame(1);
        assertEquals(1, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        // Because there weren't 2 personnel to stop, the dilemma was failed and re-seeded
        assertNotEquals(Zone.REMOVED, pinnedDown.getZone());

        // Personnel was stopped because the dilemma was failed
        assertTrue(Iterables.getOnlyElement(attemptingPersonnel).isStopped());
    }

    @Test
    public void passDilemmaTestWithTwo() throws Exception {
        initializeGame(2);
        assertEquals(2, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        // Dilemma stopped both personnel as a cost
        for (PersonnelCard personnel : attemptingPersonnel) {
            assertTrue(personnel.isStopped());
        }

        // Because there were 2 personnel to stop, the dilemma was overcome
        assertEquals(Zone.REMOVED, pinnedDown.getZone());
    }

    @Test
    public void passDilemmaWithThreeAndNoQCardTest() throws Exception {
        initializeGame(3);
        assertEquals(3, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        int personnelStopped = 0;

        // Dilemma stopped two personnel as a cost
        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                personnelStopped++;
            }
        }

        assertEquals(2, personnelStopped);

        // Because there were 2 personnel to stop, the dilemma was overcome
        assertEquals(Zone.REMOVED, pinnedDown.getZone());
    }

    @Test
    public void passDilemmaWithThreeAndQCardTest() throws Exception {
        initializeGame(3, true);
        assertEquals(3, attemptingPersonnel.size());
        assertTrue(qCard.isInPlay());
        attemptMission(P1, _mission);

        int personnelStopped = 0;

        // Dilemma stopped two personnel as a cost
        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                personnelStopped++;
            }
        }

        assertEquals(3, personnelStopped);

        // Because there were 2 personnel to stop, the dilemma was overcome
        assertEquals(Zone.REMOVED, pinnedDown.getZone());

        // Double-check that the second random selection action didn't include any targets that were already stopped
        List<SelectRandomCardsAction> randomActions = new ArrayList<>();
        for (Action action : _game.getActionsEnvironment().getPerformedActions()) {
            if (action instanceof SelectRandomCardsAction randomSelection) {
                randomActions.add(randomSelection);
            }
        }

        assertEquals(2, randomActions.size());
        assertEquals(3, randomActions.get(0).getSelectableCards(_game).size());
        assertEquals(1, randomActions.get(1).getSelectableCards(_game).size());
    }

}