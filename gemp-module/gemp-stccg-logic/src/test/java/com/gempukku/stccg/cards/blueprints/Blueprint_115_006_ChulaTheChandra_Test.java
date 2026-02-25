package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_115_006_ChulaTheChandra_Test extends AbstractAtTest {

    private MissionCard mission;
    private PersonnelCard qupta;
    private PersonnelCard korris;
    private List<PersonnelCard> matchingAttributeCards;
    private PhysicalCard chula;
    private PersonnelCard ocett;


    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission(MissionType.PLANET, Affiliation.KLINGON, P1);
        chula = builder.addSeedCardUnderMission("115_006", "Chula: The Chandra", P2, mission);


        // Korris 8/6/8 155_086
        // Maques 8/7/4 103_109
        // B'Somgh 7/6/7 155_078
        // Kahless 10/5/8 155_082
        // Qup'ta 7/7/7 204_017

        korris = builder.addCardOnPlanetSurface("155_086", "Korris", P1, mission, PersonnelCard.class);
        matchingAttributeCards = new ArrayList<>(List.of(korris));

        matchingAttributeCards.add(builder.addCardOnPlanetSurface(
                "103_109", "Maques", P1, mission, PersonnelCard.class));
        matchingAttributeCards.add(builder.addCardOnPlanetSurface(
                "155_078", "B'Somgh", P1, mission, PersonnelCard.class));
        matchingAttributeCards.add(builder.addCardOnPlanetSurface(
                "155_082", "Kahless", P1, mission, PersonnelCard.class));

        qupta = builder.addCardOnPlanetSurface("204_017", "Qup'ta", P1, mission, PersonnelCard.class);
        ocett = builder.addCardOnPlanetSurface("101_301", "Ocett", P1, mission, PersonnelCard.class);

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void matchingAttributeTest() throws Exception {
        initializeGame();
        attemptMission(P1, mission);

        for (PersonnelCard personnel : matchingAttributeCards) {
            assertTrue(chula.isBeingEncounteredBy(_game, personnel));
        }
        assertTrue(chula.isBeingEncounteredBy(_game, qupta));
        assertTrue(chula.isBeingEncounteredBy(_game, ocett));

        // Use Korris special skill to volunteer for random selection
        useGameText(P1, korris);

        // All cards with matching attributes are fine, only Qup'ta and Ocett are stopped
        for (PersonnelCard personnel : matchingAttributeCards) {
            assertFalse(personnel.isStopped());
        }

        assertTrue(qupta.isStopped());
        assertTrue(ocett.isStopped());
    }


}