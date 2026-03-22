package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_139_006_FlimFlam_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard flimFlam;
    private Collection<PersonnelCard> attemptingPersonnel;

    private void initializeGame(Quadrant quadrant, boolean canOvercomeDilemma) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        if (quadrant == Quadrant.ALPHA) {
            _mission = builder.addMission("101_154", "Excavation", P1);
        } else if (quadrant == Quadrant.DELTA) {
            _mission = builder.addMission("123_079", "Aftermath", P1);
        }
        flimFlam = builder.addSeedCardUnderMission("139_006", "Flim-Flam Artist", P2, _mission);


        /* T'Lara has Law + CUNNING 8, but no skills for the alternate requirements.
                4 copies (CUNNING=32) will fail in Alpha
                5 copies (CUNNING=40) will pass in Alpha
                6 copies (CUNNING=48) will fail in Delta
                7 copies (CUNNING=56) will pass in Delta
        */
        int copiesToBring = 4;
        if (quadrant == Quadrant.DELTA) {
            copiesToBring = copiesToBring + 2;
        }
        if (canOvercomeDilemma) {
            copiesToBring = copiesToBring + 1;
        }

        attemptingPersonnel = new ArrayList<>();
        for (int i = 0; i < copiesToBring; i++) {
            attemptingPersonnel.add(builder.addCardOnPlanetSurface(
                    "159_020", "T'Lara", P1, _mission, PersonnelCard.class));
        }
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void failDilemmaInAlphaTest() throws Exception {
        initializeGame(Quadrant.ALPHA, false);
        assertEquals(4, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        int initialHandSize = getHandSize(P2);
        assertNotNull(_game.getAwaitingDecision(P2));
        playerSaysYes(P2);

        assertEquals(initialHandSize + 1, getHandSize(P2));

        for (PersonnelCard personnel : attemptingPersonnel) {
            assertTrue(personnel.isStopped());
        }

        assertFalse(cardWasRemovedFromGame(flimFlam));
    }

    @Test
    public void failDilemmaInDeltaTest() throws Exception {
        initializeGame(Quadrant.DELTA, false);
        assertEquals(6, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        int initialHandSize = getHandSize(P2);
        assertNotNull(_game.getAwaitingDecision(P2));
        playerSaysNo(P2);

        assertEquals(initialHandSize, getHandSize(P2));

        for (PersonnelCard personnel : attemptingPersonnel) {
            assertTrue(personnel.isStopped());
        }

        assertFalse(cardWasRemovedFromGame(flimFlam));
    }

    @Test
    public void passDilemmaInAlphaTest() throws Exception {
        initializeGame(Quadrant.ALPHA, true);
        assertEquals(5, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        int initialHandSize = getHandSize(P2);
        assertNotNull(_game.getAwaitingDecision(P2));
        playerSaysNo(P2);

        assertEquals(initialHandSize, getHandSize(P2));

        for (PersonnelCard personnel : attemptingPersonnel) {
            assertFalse(personnel.isStopped());
        }

        assertTrue(cardWasRemovedFromGame(flimFlam));
    }

    @Test
    public void passDilemmaInDeltaTest() throws Exception {
        initializeGame(Quadrant.DELTA, true);
        assertEquals(7, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        int initialHandSize = getHandSize(P2);
        assertNotNull(_game.getAwaitingDecision(P2));
        playerSaysYes(P2);

        assertEquals(initialHandSize + 1, getHandSize(P2));

        for (PersonnelCard personnel : attemptingPersonnel) {
            assertFalse(personnel.isStopped());
        }

        assertTrue(cardWasRemovedFromGame(flimFlam));
    }


}