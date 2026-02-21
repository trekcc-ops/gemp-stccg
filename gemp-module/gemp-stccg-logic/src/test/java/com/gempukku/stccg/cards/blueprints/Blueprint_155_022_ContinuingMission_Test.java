package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.ST1EGame;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_022_ContinuingMission_Test extends AbstractAtTest {

    private PhysicalCard continuing;
    private PhysicalCard continuing2;
    private PhysicalCard attention;
    private PhysicalCard attention2;
    private PersonnelCard worf;
    private PersonnelCard millin;
    private PhysicalCard runabout;
    private FacilityCard outpost;
    private PersonnelCard picard;
    private PersonnelCard lopez;
    private PersonnelCard larson;
    private PhysicalCard ams;
    private PhysicalCard tarses;
    private PhysicalCard wallace;
    private PhysicalCard worfPlayerTwo;
    private PhysicalCard continuingPlayerTwo;

    private ST1EGame initializeGame(boolean includeWarpCoreCards, boolean includeMissionSpecialists,
                                    boolean includeContinuingMissionForBothPlayers)
            throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        continuing = builder.addSeedDeckCard("155_022", "Continuing Mission", P1);
        continuing2 = builder.addSeedDeckCard("155_022", "Continuing Mission", P1);
        if (includeContinuingMissionForBothPlayers) {
            continuingPlayerTwo = builder.addSeedDeckCard("155_022", "Continuing Mission", P2);
        }
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        builder.addOutpost(Affiliation.FEDERATION, P2);
        if (includeWarpCoreCards) {
            attention = builder.addDrawDeckCard("155_021", "Attention All Hands", P1);
            attention2 = builder.addDrawDeckCard("155_021", "Attention All Hands", P1);
        }
        if (includeMissionSpecialists) {
            ams = builder.addSeedDeckCard("109_063", "Assign Mission Specialists", P1);
            tarses = builder.addDrawDeckCard("101_236", "Simon Tarses", P1);
            wallace = builder.addDrawDeckCard("101_203", "Darian Wallace", P1);
        }
        worf = builder.addCardInHand("101_251", "Worf", P1, PersonnelCard.class);
        worfPlayerTwo = builder.addCardInHand("101_251", "Worf", P2, PersonnelCard.class);
        picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class);
        millin = builder.addCardInHand("163_045", "Millin", P1, PersonnelCard.class);
        runabout = builder.addCardInHand("101_331", "Runabout", P1);
        lopez = builder.addCardInHand("155_063", "Lopez", P1, PersonnelCard.class);
        larson = builder.addCardInHand("101_220", "Linda Larson", P1, PersonnelCard.class);
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
        return builder.getGame();
    }

    @Test
    public void seedOneTest() throws Exception {
        _game = initializeGame(false, false, false);
        seedCard(P1, continuing);

        // Verify that second copy of Continuing Mission can't be seeded; game has moved on past seed phase
        assertThrows(DecisionResultInvalidException.class, () -> seedCard(P1, continuing2));
        assertFalse(_game.getCurrentPhase().isSeedPhase());
    }

    @Test
    public void downloadCardTest() throws Exception {
        _game = initializeGame(true, false, false);
        seedCard(P1, continuing);
        useGameText(P1, continuing);

        // Verify that both copies of Attention All Hands are in selectable cards, but can't select both
        assertTrue(getSelectableCards(P1).containsAll(List.of(attention, attention2)));
        assertTrue(getSelectableCards(P1).size() == 2);
        assertThrows(DecisionResultInvalidException.class, () -> selectCards(P1, List.of(attention, attention2)));

        // Select card to download
        selectCard(P1, attention);
        assertTrue(attention.isInPlay());
    }

    @Test
    public void addIconTest() throws Exception {
        _game = initializeGame(false, false, false);
        List<PhysicalCard> tngCards = List.of(worf, millin, runabout);
        for (PhysicalCard card : tngCards) {
            assertFalse(card.hasIcon(_game, CardIcon.TNG_ICON));
        }
        seedCard(P1, continuing);
        for (PhysicalCard card : tngCards) {
            assertTrue(card.hasIcon(_game, CardIcon.TNG_ICON));
        }

        // Does not add TNG icon to Fed Outpost
        assertTrue(outpost.hasPropertyLogo(PropertyLogo.TNG_LOGO));
        assertFalse(outpost.hasIcon(_game, CardIcon.TNG_ICON));
    }

    @Test
    public void drawCardTest() throws Exception {
        _game = initializeGame(false, false, false);
        PhysicalCardGroup<PhysicalCard> hand = _game.getPlayer(P1).getCardGroup(Zone.HAND);
        seedCard(P1, continuing);
        int initialHandSize = hand.size();
        playCard(P1, worf);
        assertEquals(initialHandSize - 1, hand.size());
        assertTrue(worf.isInPlay());
        useGameText(P1, continuing);
        assertEquals(initialHandSize, hand.size());
    }

    @Test
    public void cannotDrawCardTest() throws Exception {
        _game = initializeGame(true, false, false);
        PhysicalCardGroup<PhysicalCard> hand = _game.getPlayer(P1).getCardGroup(Zone.HAND);
        seedCard(P1, continuing);
        useGameText(P1, continuing);
        selectCard(P1, attention);
        int initialHandSize = hand.size();

        // Play Picard; too many skills, so can't draw a card
        playCard(P1, picard);
        assertEquals(initialHandSize - 1, hand.size());
        assertTrue(picard.isInPlay());
        assertThrows(DecisionResultInvalidException.class, () -> useGameText(P1, continuing));
        assertEquals(initialHandSize - 1, hand.size());

        // Play Lopez; has a DL icon, so can't draw a card
        playCard(P1, lopez);
        assertEquals(initialHandSize - 2, hand.size());
        assertTrue(lopez.isInPlay());
        assertThrows(DecisionResultInvalidException.class, () -> useGameText(P1, continuing));
        assertEquals(initialHandSize - 2, hand.size());
    }

    @Test
    public void cannotDrawDuringSeedTest() throws Exception {
        _game = initializeGame(false, true, false);
        PhysicalCardGroup<PhysicalCard> hand = _game.getPlayer(P1).getCardGroup(Zone.HAND);
        int initialHandSize = hand.size();
        seedCard(P1, continuing);
        seedCard(P1, ams);
        useGameText(P1, ams);
        selectCards(P1, List.of(tarses, wallace));
        assertEquals(Phase.START_OF_TURN, _game.getCurrentPhase());

        // Verify that only the starting hand was drawn; no additional card draws
        assertEquals(initialHandSize + _game.getFormat().getHandSize(), hand.size());
    }

    @Test
    public void cannotDrawTwiceTest() throws Exception {
        _game = initializeGame(true, false, false);
        PhysicalCardGroup<PhysicalCard> hand = _game.getPlayer(P1).getCardGroup(Zone.HAND);
        seedCard(P1, continuing);
        useGameText(P1, continuing);
        selectCard(P1, attention);
        int initialHandSize = hand.size();

        // Play Worf to draw a card
        playCard(P1, worf);
        assertEquals(initialHandSize - 1, hand.size());
        assertTrue(worf.isInPlay());
        useGameText(P1, continuing);
        assertEquals(initialHandSize, hand.size());

        // Play Linda Larson; can't draw a card twice in one turn
        playCard(P1, larson);
        assertTrue(larson.isInPlay());
        assertEquals(initialHandSize - 1, hand.size());
        assertThrows(DecisionResultInvalidException.class, () -> useGameText(P1, continuing));
        assertEquals(initialHandSize - 1, hand.size());
    }

    @Test
    public void cannotDrawWhenOpponentPlays() throws Exception {
        _game = initializeGame(false, false, true);
        PhysicalCardGroup<PhysicalCard> hand = _game.getPlayer(P1).getCardGroup(Zone.HAND);
        seedCard(P1, continuing);
        seedCard(P2, continuingPlayerTwo);

        skipToNextTurnAndPhase(P2, Phase.CARD_PLAY);
        int initialHandSize = hand.size();

        assertEquals(P2, _game.getCurrentPlayerId());
        playCard(P2, worfPlayerTwo);
        assertTrue(worfPlayerTwo.hasIcon(_game, CardIcon.TNG_ICON));
        assertThrows(DecisionResultInvalidException.class, () -> useGameText(P1, continuing));
        assertEquals(initialHandSize, hand.size());
    }

}