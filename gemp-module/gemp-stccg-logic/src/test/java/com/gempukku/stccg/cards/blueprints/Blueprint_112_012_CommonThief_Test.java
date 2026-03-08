package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.GameRandomizer;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_112_012_CommonThief_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard commonThief;
    private List<PersonnelCard> targetPersonnel;
    private List<PhysicalCard> attemptingCards;
    private List<EquipmentCard> equipmentPresent;

    private void initializeGame(boolean includeEquipment, boolean includeTargetPersonnel,
                                GameRandomizer randomizer) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players, randomizer);
        _mission = builder.addMission("101_154", "Excavation", P1);
        commonThief = builder.addSeedCardUnderMission("112_012", "Common Thief", P1, _mission);

        targetPersonnel = new ArrayList<>();
        attemptingCards = new ArrayList<>();
        equipmentPresent = new ArrayList<>();

        attemptingCards.add(builder.addCardOnPlanetSurface("204_023", "Jacen", P1, _mission));

        if (includeEquipment) {
            for (int i = 0; i <3; i++) {
                EquipmentCard medKit = builder.addCardOnPlanetSurface(
                        "101_060", "Medical Kit", P1, _mission, EquipmentCard.class);
                equipmentPresent.add(medKit);
                attemptingCards.add(medKit);
            }
        }

        if (includeTargetPersonnel) {
            for (int i = 0; i < 3; i++) {
                PersonnelCard chagrith = builder.addCardOnPlanetSurface(
                        "202_057", "Chagrith", P1, _mission, PersonnelCard.class);
                targetPersonnel.add(chagrith);
                attemptingCards.add(chagrith);
            }
        }

        // Add opposing cards to make sure they're not included in selections
        builder.addCardOnPlanetSurface("101_060", "Medical Kit", P2, _mission, EquipmentCard.class);
        builder.addCardOnPlanetSurface("202_057", "Chagrith", P2, _mission, PersonnelCard.class);


        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void equipmentPresentTest() throws Exception {
        GameRandomizer randomizer = Mockito.mock(GameRandomizer.class);
        initializeGame(true, true, randomizer);
        EquipmentCard equipmentToDiscard = equipmentPresent.getFirst();
        assertNotNull(equipmentToDiscard);

        Mockito.when(randomizer.getRandomItemsFromList(Mockito.anyCollection(), Mockito.eq(1)))
                .thenReturn(List.of(equipmentToDiscard));

        attemptMission(P1, _mission);
        assertTrue(equipmentToDiscard.isInDiscard(_game));
        assertFalse(equipmentToDiscard.isInPlay());

        for (PhysicalCard card : attemptingCards) {
            assertTrue(card == equipmentToDiscard || card.isInPlay());
            assertFalse(((StoppableCard) card).isStopped());
        }

        assertEquals(Zone.REMOVED, commonThief.getZone());
    }

    @Test
    public void targetPersonnelPresentTest() throws Exception {
        initializeGame(false, true, new GameRandomizer());
        attemptMission(P1, _mission);

        assertTrue(selectableCardsAre(P2, targetPersonnel));

        PersonnelCard cardToKill = targetPersonnel.getFirst();
        selectCard(P2, cardToKill);
        assertTrue(personnelWasKilledAndDiscarded(cardToKill));

        for (PhysicalCard card : attemptingCards) {
            assertFalse(((StoppableCard) card).isStopped());
        }

        assertEquals(Zone.REMOVED, commonThief.getZone());
    }

    @Test
    public void neitherTest() throws Exception {
        initializeGame(false, false, new GameRandomizer());
        attemptMission(P1, _mission);

        for (PhysicalCard card : attemptingCards) {
            assertTrue(card.isInPlay());
            assertFalse(((StoppableCard) card).isStopped());
        }

        assertEquals(Zone.REMOVED, commonThief.getZone());
    }

}