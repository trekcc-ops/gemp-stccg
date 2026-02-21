package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_152_030_WinnAdami_Test extends AbstractAtTest {

    PhysicalCard dipContactInHand;
    PersonnelCard theOne;
    PersonnelCard gozar;
    PersonnelCard picard;
    EquipmentCard yourPhaser1;
    EquipmentCard yourPhaser2;
    EquipmentCard opponentsPhaser;
    PhysicalCard beverlyInHand;
    PhysicalCard mvil;
    PhysicalCard aletia;
    PhysicalCard beverlyInDiscard;
    PersonnelCard winn;
    PhysicalCard dipContactInDeck;

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.skipStartingHands();
        MissionCard mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        MissionCard mission2 = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P2);
        theOne = builder.addCardOnPlanetSurface("163_047", "The One", P1, mission, PersonnelCard.class);
        gozar = builder.addCardOnPlanetSurface("155_070", "Gozar", P1, mission2, PersonnelCard.class);
        winn = builder.addCardOnPlanetSurface("152_030", "Winn Adami", P1, mission, PersonnelCard.class);
        picard = builder.addCardOnPlanetSurface("101_215", "Jean-Luc Picard", P1, mission, PersonnelCard.class);
        dipContactInHand = builder.addCardInHand("152_022", "Diplomatic Contact", P1);
        dipContactInDeck = builder.addDrawDeckCard("152_022", "Diplomatic Contact", P1);
        yourPhaser1 = builder.addCardInHand("112_046", "Bajoran Phaser", P1, EquipmentCard.class);
        yourPhaser2 = builder.addCardInHand("112_046", "Bajoran Phaser", P1, EquipmentCard.class);
        opponentsPhaser = builder.addCardInHand("112_046", "Bajoran Phaser", P2, EquipmentCard.class);
        beverlyInHand = builder.addCardInHand("155_054", "Beverly", P1);
        mvil = builder.addCardInDiscard("172_040", "M'vil", P1);
        aletia = builder.addCardInDiscard("204_010", "Aletia", P1);
        beverlyInDiscard = builder.addCardInDiscard("155_054", "Beverly", P1);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void playCardTest() throws Exception {
        initializeGame();
        useGameText(P1, winn);
        assertTrue(selectableCardsAre(P1, List.of(dipContactInHand, dipContactInDeck)));
        selectCard(P1, dipContactInDeck);

        // Verify that you can download Diplomatic Contact to Winn or The One because they are at Winn's location
        // Should not be able to download to Gozar at the other mission
        assertTrue(selectableCardsAre(P1, List.of(winn, theOne)));
    }

}