package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.playcard.DownloadReportableCardToDestinationAction;
import com.gempukku.stccg.actions.playcard.SelectAndReportForFreeCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_021_AAH_Test extends AbstractAtTest {

    private PersonnelCard lopez1;
    private List<PhysicalCard> playableCards;
    private List<PhysicalCard> unplayableCards;
    private PhysicalCard attention;
    private ShipCard runaboutInHand;
    private PhysicalCard galaxy;
    private FacilityCard yourFedOutpost;
    private FacilityCard opponentsFedOutpost;
    private MissionCard mission;
    private MissionCard mission2;
    private FacilityCard yourKlingonOutpost;
    private ShipCard runaboutInPlay;
    private FacilityCard yourGammaFedOutpost;
    private MissionCard gammaMission;
    private FacilityCard yourNonAlignedOutpost;

    @SuppressWarnings("SpellCheckingInspection")
    public void initializeGame(boolean larsonInPlay, boolean multipleDestinations)
            throws CardNotFoundException, DecisionResultInvalidException,
            InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        builder.addCardToCoreAsSeeded("155_022", "Continuing Mission", P1);
        attention = builder.addCardToCoreAsSeeded("155_021", "Attention All Hands", P1);
        yourFedOutpost = builder.addOutpost(Affiliation.FEDERATION, P1);
        if (multipleDestinations) {
            opponentsFedOutpost = builder.addOutpost(Affiliation.FEDERATION, P2);
            mission2 = builder.addMission(MissionType.PLANET, Affiliation.KLINGON, P2);
            yourKlingonOutpost = builder.addOutpost(Affiliation.KLINGON, P1, mission2);
            runaboutInPlay = builder.addShipInSpace("101_331", "Runabout", P1, mission);
            gammaMission = builder.addMission("112_105", "Access Relay Station", P1);
            yourGammaFedOutpost = builder.addOutpost(Affiliation.FEDERATION, P1, gammaMission);
            yourNonAlignedOutpost = builder.addOutpost(Affiliation.NON_ALIGNED, P1, mission2);
        }

        // playable
        lopez1 = builder.addCardInHand("155_063", "Lopez", P1, PersonnelCard.class);
        PhysicalCard lopez2 = builder.addCardInHand("155_063", "Lopez", P1, PersonnelCard.class);
        playableCards = new ArrayList<>();
        playableCards.addAll(List.of(lopez1, lopez2));

        // not playable
        PhysicalCard picard = builder.addCardInHand("101_215", "Jean-Luc Picard", P1, PersonnelCard.class); // unique
        PhysicalCard jace = builder.addCardInHand("112_207", "Jace Michaels", P1, PersonnelCard.class); // no TNG icon
        PhysicalCard rmal = builder.addCardInHand("116_095", "R'Mal", P1, PersonnelCard.class); // no matching outpost
        unplayableCards = new ArrayList<>();
        unplayableCards.addAll(List.of(picard, jace, rmal));

        PhysicalCard larsonInHand = builder.addCardInHand("101_220", "Linda Larson", P1, PersonnelCard.class);
        runaboutInHand = builder.addCardInHand("101_331", "Runabout", P1, ShipCard.class);
        galaxy = builder.addDrawDeckCard("101_336", "U.S.S. Galaxy", P1);

        if (larsonInPlay) {
            builder.addCardAboardShipOrFacility("101_220", "Linda Larson", P1, yourFedOutpost, PersonnelCard.class);
            unplayableCards.add(larsonInHand);
        } else {
            playableCards.add(larsonInHand);
        }

        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void testWithAndWithoutLarsonInPlay() throws Exception {
        testThis(true);
        testThis(false);
    }

    public void testThis(boolean larsonInPlay) throws Exception {
        initializeGame(larsonInPlay, false);

        selectAction(SelectAndReportForFreeCardAction.class, attention, P1);
        for (PhysicalCard card : playableCards) {
            assertTrue(getSelectableCards(P1).contains(card));
        }

        for (PhysicalCard card : unplayableCards) {
            assertFalse(getSelectableCards(P1).contains(card));
        }

        // Select a card for card play
        selectCard(P1, lopez1);
        assertTrue(lopez1.isInPlay());

        assertThrows(DecisionResultInvalidException.class, () -> selectAction(SelectAndReportForFreeCardAction.class, attention, P1));
    }

    @Test
    public void downloadShipTest() throws Exception {
        initializeGame(false, false);
        assertEquals(1, _game.getGameState().getNormalCardPlaysAvailable(P1));
        selectAction(DownloadReportableCardToDestinationAction.class, attention, P1);
        assertTrue(getSelectableCards(P1).containsAll(List.of(galaxy, runaboutInHand)));
        selectCard(P1, runaboutInHand);
        assertTrue(runaboutInHand.isDockedAtCardId(yourFedOutpost.getCardId()));
        assertEquals(0, _game.getGameState().getNormalCardPlaysAvailable(P1));
    }

    @Test
    public void validDestinationsTest() throws Exception {
        initializeGame(true, true);
        selectAction(SelectAndReportForFreeCardAction.class, attention, P1);
        selectCard(P1, lopez1);
        assertFalse(lopez1.isInPlay());

        // Can report to your Fed outpost in either quadrant, but not any other destinations
        assertTrue(selectableCardsAre(List.of(yourFedOutpost, yourGammaFedOutpost), P1));
    }

}