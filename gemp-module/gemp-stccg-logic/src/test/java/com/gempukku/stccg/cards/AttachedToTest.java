package com.gempukku.stccg.cards;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.movecard.DockAction;
import com.gempukku.stccg.actions.movecard.UndockAction;
import com.gempukku.stccg.actions.movecard.WalkCardsAction;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AttachedToTest extends AbstractAtTest {

    FacilityCard outpost;
    ShipCard runabout;
    PersonnelCard picard;
    PersonnelCard beverly;
    PhysicalCard phaser;
    MissionCard mission;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        builder.addSeedCardUnderMission("101_015", "Armus: Skin of Evil", P2, mission);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1, mission);
        runabout = builder.addDockedShip("101_331", "Runabout", P1, outpost);
        picard = builder.addCardAboardShipOrFacility(
                "101_215", "Jean-Luc Picard", P1, runabout, PersonnelCard.class);
        beverly = builder.addCardInHand("155_054", "Beverly", P1, PersonnelCard.class);
        phaser = builder.addDrawDeckCard("101_064", "Starfleet Type II Phaser", P1);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void dockingTest() throws Exception {
        initializeGame();
        assertTrue(runabout.isDocked());

        performAction(P1, UndockAction.class, runabout);
        assertFalse(runabout.isDocked());
        assertEquals(mission, runabout.getAttachedTo(_game));

        performAction(P1, DockAction.class, runabout);
        assertEquals(outpost, runabout.getAttachedTo(_game));
    }

    @Test
    public void beamingAndWalkingTest() throws Exception {
        initializeGame();
        assertEquals(runabout, picard.getAttachedTo(_game));
        beamCards(P1, runabout, List.of(picard), outpost);
        assertEquals(outpost, picard.getAttachedTo(_game));
        performAction(P1, WalkCardsAction.class, outpost);
        assertEquals(runabout, picard.getAttachedTo(_game));
    }

    @Test
    public void playingCardsTest() throws Exception {
        initializeGame();
        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        assertNull(beverly.getAttachedTo(_game));
        assertNull(phaser.getAttachedTo(_game));

        // Report Beverly to outpost
        playCard(P1, beverly);
        assertEquals(outpost, beverly.getAttachedTo(_game));

        // Special download phaser to mission
        downloadCard(P1, phaser);
        selectCard(P1, mission);
        assertEquals(mission, phaser.getAttachedTo(_game));
    }

    @Test
    public void cardLeavesPlayTest() throws Exception {
        initializeGame();
        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        playCard(P1, beverly);
        downloadCard(P1, phaser);
        selectCard(P1, mission);

        // Attempt mission. Beverly is killed by Armus.
        beamCards(P1, outpost, List.of(beverly), mission);
        assertEquals(mission, phaser.getAttachedTo(_game));
        assertEquals(mission, beverly.getAttachedTo(_game));
        attemptMission(P1, mission);
        assertNull(beverly.getAttachedTo(_game));
        assertEquals(mission, phaser.getAttachedTo(_game));
    }

}