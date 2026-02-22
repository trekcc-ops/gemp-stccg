package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_012_AnaphasicOrganism_Test extends AbstractAtTest {

    private PersonnelCard calloway;
    private PersonnelCard data;
    private PersonnelCard loews;
    private PersonnelCard loews2;
    private MissionCard _mission;
    private PhysicalCard organism;

    private void initializeGame(boolean bringFemales, boolean canOvercome, boolean requireTieBreaker)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_154", "Excavation", P1);
        organism = builder.addSeedCardUnderMission("101_012", "Anaphasic Organism", P2, _mission);

        // Calloway 7-5-3
        // Karen Loews 7-6-4

        calloway = (bringFemales) ?
                builder.addCardOnPlanetSurface("101_201", "Calloway", P1, _mission, PersonnelCard.class) : null;
        loews = (bringFemales) ?
                builder.addCardOnPlanetSurface("112_210", "Karen Loews", P1, _mission, PersonnelCard.class) : null;

        data = builder.addCardOnPlanetSurface("101_204", "Data", P1, _mission, PersonnelCard.class);

        loews2 = (requireTieBreaker && bringFemales) ?
            builder.addCardOnPlanetSurface("112_210", "Karen Loews", P1, _mission, PersonnelCard.class) :
            null;

        PersonnelCard wallace = (canOvercome) ?
                builder.addCardOnPlanetSurface("101_203", "Darian Wallace", P1, _mission, PersonnelCard.class) :
                null;

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }


    @Test
    public void discardLoewsTest() throws Exception {
        // Away team has Data, Calloway, and Loews. Loews is the female with highest attributes.
        initializeGame(true,false, false);
        attemptMission(P1, _mission);

        // Loews is discarded, but not killed
        assertFalse(personnelWasKilled(loews));
        assertTrue(loews.isInDiscard(_game));

        assertEquals(Zone.REMOVED, organism.getZone());
        assertTrue(data.isStopped());
        assertTrue(calloway.isStopped());
    }

    @Test
    public void opponentBreaksTieTest() throws Exception {
        // If two Loews are present, opponent has to pick one
        initializeGame(true,false, true);
        attemptMission(P1, _mission);
        assertNotEquals(Zone.REMOVED, organism.getZone());
        assertTrue(selectableCardsAre(P2, List.of(loews, loews2)));
        selectCard(P2, loews2);
        assertTrue(loews2.isInDiscard(_game));
        assertEquals(Zone.REMOVED, organism.getZone());
        assertTrue(loews.isStopped());
        assertTrue(data.isStopped());
        assertTrue(calloway.isStopped());
    }

    @Test
    public void overcomeDilemmaTest() throws Exception {
        /* If Away Team has SECURITY + MEDICAL, dilemma is overcome and nobody is discarded or stopped. */
        initializeGame(true,true, false);
        attemptMission(P1, _mission);
        assertEquals(Zone.REMOVED, organism.getZone());
        assertFalse(loews.isStopped());
        assertFalse(data.isStopped());
        assertFalse(calloway.isStopped());
    }

    @Test
    public void noFemalesToDiscardTest() throws Exception {
        // If Away Team brings no females, nobody is discarded but the dilemma is still failed
        initializeGame(false,false, false);
        assertEquals(1, _game.getGameState().getAwayTeamForCard(data).size());

        attemptMission(P1, _mission);
        assertEquals(Zone.REMOVED, organism.getZone());
        assertTrue(data.isStopped());
        assertFalse(data.isInDiscard(_game));
    }

}