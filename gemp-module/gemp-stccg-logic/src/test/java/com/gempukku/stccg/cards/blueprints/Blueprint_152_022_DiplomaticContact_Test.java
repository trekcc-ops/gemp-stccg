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

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_152_022_DiplomaticContact_Test extends AbstractAtTest {

    PhysicalCard dipContact;
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

    private void initializeGame() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.skipStartingHands();
        MissionCard mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        theOne = builder.addCardOnPlanetSurface("163_047", "The One", P1, mission, PersonnelCard.class);
        gozar = builder.addCardOnPlanetSurface("155_070", "Gozar", P1, mission, PersonnelCard.class);
        picard = builder.addCardOnPlanetSurface("101_215", "Jean-Luc Picard", P1, mission, PersonnelCard.class);
        dipContact = builder.addCardInHand("152_022", "Diplomatic Contact", P1);
        yourPhaser1 = builder.addCardInHand("112_046", "Bajoran Phaser", P1, EquipmentCard.class);
        yourPhaser2 = builder.addCardInHand("112_046", "Bajoran Phaser", P1, EquipmentCard.class);
        opponentsPhaser = builder.addCardInHand("112_046", "Bajoran Phaser", P2, EquipmentCard.class);
        beverlyInHand = builder.addCardInHand("155_054", "Beverly", P1);
        mvil = builder.addCardInDiscard("172_040", "M'vil", P1);
        aletia = builder.addCardInDiscard("204_010", "Aletia", P1);
        beverlyInDiscard = builder.addCardInDiscard("155_054", "Beverly", P1);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void playCardTest() throws Exception {
        initializeGame();
        playCard(P1, dipContact);
        assertTrue(selectableCardsAre(P1, List.of(gozar, theOne)));
        selectCard(P1, theOne);
        assertTrue(dipContact.isInPlay());
        assertEquals(theOne, dipContact.getAtopCard());
        assertEquals(0, _game.getGameState().getNormalCardPlaysAvailable(P1));
    }

    @Test
    public void useTextForSameAffiliationTest() throws Exception {
        initializeGame();
        playCard(P1, dipContact);
        selectCard(P1, theOne);
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
        useGameText(P1, dipContact);

        // Verify you can discard either phaser
        assertTrue(selectableCardsAre(P1, List.of(yourPhaser1, yourPhaser2)));
        selectCard(P1, yourPhaser1);

        // Verify cards you can retrieve from discard pile
        assertTrue(selectableCardsAre(P1, List.of(mvil, aletia, beverlyInDiscard)));
        selectCard(P1, mvil);

        // Verify Diplomatic Contact was discarded
        assertFalse(dipContact.isInPlay());
        assertTrue(dipContact.isInDiscard(_game));
    }

    @Test
    public void useTextForDifferentAffiliationTest() throws Exception {
        initializeGame();
        playCard(P1, dipContact);
        selectCard(P1, theOne);
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
        useGameText(P1, dipContact);

        // Verify you can discard either phaser
        assertTrue(selectableCardsAre(P1, List.of(yourPhaser1, yourPhaser2)));
        selectCard(P1, yourPhaser1);

        // Verify cards you can retrieve from discard pile
        assertTrue(selectableCardsAre(P1, List.of(mvil, aletia, beverlyInDiscard)));
        selectCard(P1, beverlyInDiscard);

        // Verify Diplomatic Contact was not discarded
        assertTrue(dipContact.isInPlay());
    }


}