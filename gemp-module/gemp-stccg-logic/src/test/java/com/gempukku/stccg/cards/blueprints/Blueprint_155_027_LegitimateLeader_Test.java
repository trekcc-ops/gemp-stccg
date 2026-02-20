package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.playcard.DownloadAction;
import com.gempukku.stccg.actions.playcard.SelectAndReportForFreeCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_027_LegitimateLeader_Test extends AbstractAtTest {

    private enum WhereIsGowron {
        notInPlay, onPlanet, onYourFacilityWithIntruders, onOpponentsShip
    };
    private FacilityCard yourAlphaKlingonOutpost;
    private FacilityCard yourGammaKlingonOutpost;
    private FacilityCard yourEmptyNonAlignedOutpost;
    private MissionCard klingonMission;
    private PhysicalCard legitimateLeader;
    private PhysicalCard divok;
    private PersonnelCard multiAffilKlingon;
    private List<PhysicalCard> downloadableGowrons;

    @SuppressWarnings("SpellCheckingInspection")
    public void initializeGame(WhereIsGowron whereIsGowron, boolean includeCompatibleFacilities)
            throws CardNotFoundException, DecisionResultInvalidException,
            InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addCardToCoreAsSeeded("155_022", "Continuing Mission", P1);
        legitimateLeader = builder.addCardToCoreAsSeeded("155_027", "Legitimate Leader of the Empire", P1);
        klingonMission = builder.addMission(MissionType.PLANET, Affiliation.KLINGON, P1);
        MissionCard nonAlignedMission1 = builder.addMission("155_038", "Encounter at Farpoint", P1);
        MissionCard mission3 = builder.addMission(MissionType.PLANET, Affiliation.KLINGON, P1);

        downloadableGowrons = List.of(
            builder.addDrawDeckCard("101_261", "Gowron", P1),
            builder.addDrawDeckCard("101_261", "Gowron", P1),
            builder.addCardInHand("101_261", "Gowron", P1)
        );


        builder.addDrawDeckCard("101_261", "Gowron", P2);

        divok = builder.addCardInHand("101_256", "Divok", P1);
        multiAffilKlingon = builder.addCardInHand("991_007", "Dummy 1E Multi-Affiliation Klingon", P1,
                PersonnelCard.class);
        PhysicalCard opponentsDivok = builder.addCardInHand("101_256", "Divok", P2);
        PhysicalCard lursa = builder.addCardInHand("101_280", "Lursa", P1);
        PhysicalCard mvil = builder.addCardInHand("172_040", "M'vil", P1);


        MissionCard gammaMission = builder.addMission("112_105", "Access Relay Station", P1);

        if (includeCompatibleFacilities) {
            yourAlphaKlingonOutpost = builder.addOutpost(Affiliation.KLINGON, P1, klingonMission);
            yourEmptyNonAlignedOutpost = builder.addOutpost(Affiliation.NON_ALIGNED, P1, nonAlignedMission1);
            yourGammaKlingonOutpost = builder.addOutpost(Affiliation.KLINGON, P1, gammaMission);
        }

        FacilityCard yourNonAlignedOutpostWithFedAboard = builder.addOutpost(Affiliation.NON_ALIGNED, P1, mission3);
        builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1,
                yourNonAlignedOutpostWithFedAboard, PersonnelCard.class);

        PhysicalCard gowronInPlay = switch(whereIsGowron) {
            case notInPlay -> null;
            case onPlanet -> builder.addCardOnPlanetSurface("101_261", "Gowron", P1, klingonMission);
            case onYourFacilityWithIntruders -> builder.addCardAboardShipOrFacility("101_261",
                    "Gowron", P1, yourNonAlignedOutpostWithFedAboard, PersonnelCard.class);
            case onOpponentsShip -> null;
        };

        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void cannotSeedTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        legitimateLeader = builder.addSeedDeckCard("155_027", "Legitimate Leader of the Empire", P1);
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, legitimateLeader));
    }

    @Test
    public void playCardTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        legitimateLeader = builder.addCardInHand("155_027", "Legitimate Leader of the Empire", P1);
        assertTrue(legitimateLeader.hasIcon(_game, CardIcon.WARP_CORE));
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
        assertDoesNotThrow(() -> playCard(P1, legitimateLeader));
    }

    @Test
    public void downloadGowronTest() throws Exception {
        initializeGame(WhereIsGowron.notInPlay, true);
        assertEquals(1, getSelectableActionsOfClass(P1, DownloadAction.class).size());
        performAction(P1, DownloadAction.class, legitimateLeader);

        PhysicalCard gowronToDownload = downloadableGowrons.getFirst();

        // Verify you can download any of your copies, but not opponent's copy
        assertTrue(selectableCardsAre(P1, downloadableGowrons));
        selectCard(P1, gowronToDownload);
        assertFalse(gowronToDownload.isInPlay());

        // Verify you can download to either of your Klingon outposts, but nowhere else
        assertTrue(selectableCardsAre(P1,
                List.of(yourAlphaKlingonOutpost, yourGammaKlingonOutpost, yourEmptyNonAlignedOutpost)));
        selectCard(P1, yourAlphaKlingonOutpost);
        assertTrue(gowronToDownload.isInPlay());
        assertTrue(gowronToDownload.isAboard(yourAlphaKlingonOutpost));
    }

    @Test
    public void cannotPerformActionsTest() throws Exception {
        initializeGame(WhereIsGowron.onPlanet, true);
        assertEquals(0, getSelectableActionsOfClass(P1, DownloadAction.class).size());

        initializeGame(WhereIsGowron.notInPlay, false);
        assertEquals(0, getSelectableActionsOfClass(P1, DownloadAction.class).size());
        assertEquals(0, getSelectableActionsOfClass(P1, SelectAndReportForFreeCardAction.class).size());

        initializeGame(WhereIsGowron.notInPlay, true);
        assertNotEquals(0, getSelectableActionsOfClass(P1, DownloadAction.class).size());
        assertEquals(0, getSelectableActionsOfClass(P1, SelectAndReportForFreeCardAction.class).size());
    }

    @Test
    public void reportForFreeTest() throws Exception {
        initializeGame(WhereIsGowron.onPlanet, true);
        assertEquals(1, getSelectableActionsOfClass(P1, SelectAndReportForFreeCardAction.class).size());
        performAction(P1, SelectAndReportForFreeCardAction.class, legitimateLeader);
        assertTrue(selectableCardsAre(P1, List.of(divok, multiAffilKlingon)));
        selectCard(P1, multiAffilKlingon);
        assertTrue(selectableCardsAre(P1, List.of(klingonMission, yourAlphaKlingonOutpost, yourGammaKlingonOutpost)));
        selectCard(P1, klingonMission);

        /* Verify that the player was not allowed to select non-aligned for the personnel's affiliation, since
            Legitimate Leader specifies the card being reported must be Klingon.
         */
        assertTrue(multiAffilKlingon.isInPlay());
        assertEquals(Affiliation.KLINGON, Iterables.getOnlyElement(multiAffilKlingon.getCurrentAffiliations()));

        // Didn't use normal card play
        assertEquals(1, _game.getGameState().getNormalCardPlaysAvailable(P1));

        // Cannot use it again even though there is another valid target, because it's a once per turn action
        assertEquals(0, getSelectableActionsOfClass(P1, SelectAndReportForFreeCardAction.class).size());

        // Can't use on opponent's turn
        skipToNextTurnAndPhase(P2, Phase.CARD_PLAY);
        assertEquals(0, getSelectableActionsOfClass(P1, SelectAndReportForFreeCardAction.class).size());

        // Can use it on your next turn
        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        assertEquals(1, getSelectableActionsOfClass(P1, SelectAndReportForFreeCardAction.class).size());
    }

}