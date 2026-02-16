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

public class Blueprint_101_014_Archer_Test extends AbstractAtTest {

    private PersonnelCard wilkins;
    private MissionCard _mission;
    private PhysicalCard archer;
    private PersonnelCard wallace;
    private PersonnelCard wilkins2;

    private void initializeGame(int wilkinsCount, boolean includeMedTricorder)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_154", "Excavation", P1);
        archer = builder.addSeedCardUnderMission("101_014", "Archer", P2, _mission);
        wilkins = builder.addCardOnPlanetSurface("109_111", "Richard Wilkins", P1, _mission, PersonnelCard.class);
        if (wilkinsCount == 2) {
            wilkins2 = builder.addCardOnPlanetSurface("109_111", "Richard Wilkins", P1, _mission, PersonnelCard.class);
        }
        if (includeMedTricorder) {
            builder.addCardOnPlanetSurface("101_061", "Medical Tricorder", P1, _mission);
        }
        wallace = builder.addCardOnPlanetSurface("101_203", "Darian Wallace", P1, _mission, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }


    @Test
    public void killWilkinsTest() throws Exception {
        /* If Away Team is Richard Wilkins (7+7+5) and Darian Wallace (7+5+6),
        Wilkins has the highest attributes and will be killed */
        initializeGame(1, false);
        attemptMission(P1, _mission);
        assertTrue(personnelWasKilled(wilkins));
        assertEquals(Zone.REMOVED, archer.getZone());
        assertTrue(wallace.isStopped());
    }

    @Test
    public void opponentBreaksTieTest() throws Exception {
        /* If Away Team has two copies of Richard Wilkins (7+7+5) and Darian Wallace (7+5+6),
        Wilkins has the highest attributes. Player two must select which one will be killed. */
        initializeGame(2, false);
        attemptMission(P1, _mission);
        assertFalse(personnelWasKilled(wilkins));
        assertNotEquals(Zone.REMOVED, archer.getZone());
        assertTrue(selectableCardsAre(List.of(wilkins, wilkins2), P2));
        selectCard(P2, wilkins2);
        assertTrue(personnelWasKilled(wilkins2));
        assertEquals(Zone.REMOVED, archer.getZone());
        assertTrue(wilkins.isStopped());
        assertTrue(wallace.isStopped());
    }

    @Test
    public void overcomeDilemmaTest() throws Exception {
        /* If Away Team has SECURITY + MEDICAL, dilemma is overcome and nobody is killed or stopped. */
        initializeGame(1, true);
        attemptMission(P1, _mission);
        assertFalse(personnelWasKilled(wilkins));
        assertFalse(personnelWasKilled(wallace));
        assertEquals(Zone.REMOVED, archer.getZone());
        assertFalse(wilkins.isStopped());
        assertFalse(wallace.isStopped());
    }

}