package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.playcard.DownloadAction;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_161_015_IllegitimateLeader_Test extends AbstractAtTest {

    private FacilityCard yourAlphaKlingonOutpost;
    private FacilityCard yourDeltaKlingonOutpost;
    private FacilityCard yourNonAlignedOutpost;
    private PhysicalCard illegitimateLeader;
    private PersonnelCard gowron;
    private List<PhysicalCard> downloadablePersonnel;
    private List<PhysicalCard> reportablePersonnel;
    private MissionCard missionWithYourBetor;
    private PhysicalCard lursa;

    @SuppressWarnings("SpellCheckingInspection")
    public void initializeGame(boolean includeContinuingMission) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);

        if (includeContinuingMission) {
            builder.addCardToCoreAsSeeded("155_022", "Continuing Mission", P1);
        }
        illegitimateLeader =
                builder.addCardToCoreAsSeeded("161_015", "Illegitimate Leader of the Empire", P1);

        missionWithYourBetor = builder.addMission(MissionType.PLANET, Affiliation.KLINGON, P1);
        MissionCard missionWithOpponentsBetor =
                builder.addMission("155_038", "Encounter at Farpoint", P1);
        MissionCard deltaMission = builder.addMission("123_079", "Aftermath", P1);
        yourAlphaKlingonOutpost = builder.addFacility("101_105", P1, missionWithYourBetor);
        FacilityCard opponentsAlphaKlingonOutpost = builder.addFacility("101_105", P2, missionWithYourBetor);
        yourDeltaKlingonOutpost = builder.addFacility("101_105", P1, deltaMission);
        yourNonAlignedOutpost = builder.addFacility("111_009", P1, missionWithOpponentsBetor);

        PersonnelCard yourBetorOnPlanet =
                builder.addCardOnPlanetSurface("101_252", "B'Etor", P1, missionWithYourBetor, PersonnelCard.class);
        PersonnelCard opponentsBetorOnPlanet =
                builder.addCardOnPlanetSurface("101_252", "B'Etor", P2, missionWithOpponentsBetor, PersonnelCard.class);

        // cards that can be downloaded
        PhysicalCard yourDurasInHand = builder.addCardInHand("101_258", "Duras", P1);
        PhysicalCard gowronInDeck = builder.addDrawDeckCard("101_261", "Gowron", P1);
        PhysicalCard opponentsDurasInDeck = builder.addDrawDeckCard("101_258", "Duras", P2);

        gowron = builder.addCardInHand("101_261", "Gowron", P1, PersonnelCard.class);
        PhysicalCard thei = builder.addCardInHand("101_324", "Thei", P1);
        lursa = builder.addCardInHand("101_280", "Lursa", P1);
        PhysicalCard telok = builder.addCardInHand("122_098", "Telok", P1);
        PhysicalCard gorta = builder.addCardInHand("101_297", "Gorta", P1);
        PhysicalCard worf = builder.addCardInHand("101_251", "Worf", P1);
        downloadablePersonnel = (includeContinuingMission) ?
                List.of(yourDurasInHand, gowronInDeck, gowron, thei, lursa) : new ArrayList<>();
        reportablePersonnel = (includeContinuingMission) ?
                List.of(yourDurasInHand, lursa) : new ArrayList<>();

        builder.setPhase(Phase.CARD_PLAY);
        _game = builder.startGame();
    }

    @Test
    public void canPlayButNotSeedTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        PhysicalCard continuing = builder.addSeedDeckCard("155_022", "Continuing Mission", P1);
        PhysicalCard leaderToSeed =
                builder.addSeedDeckCard("161_015", "Illegitimate Leader of the Empire", P1);
        PhysicalCard leaderToDownload =
                builder.addDrawDeckCard("161_015", "Illegitimate Leader of the Empire", P1);
        builder.setPhase(Phase.SEED_FACILITY);
        _game = builder.startGame();

        assertThrows(DecisionResultInvalidException.class, () -> seedCard(P1, leaderToSeed));
        seedCard(P1, continuing);
        playerSaysYes(P1);

        // leaderToDownload should be automatically selected since it's the only downloadable card
        assertTrue(leaderToDownload.isInPlay());
    }

    @Test
    public void noTngCardsTest() throws Exception {
        // Can't download or report for free if there are no TNG cards
        initializeGame(false);
        assertThrows(DecisionResultInvalidException.class,
                () -> performAction(P1, DownloadAction.class, illegitimateLeader));
    }

    @Test
    public void downloadPersonnelTest() throws Exception {
        initializeGame(true);
        assertEquals(1, _game.getGameState().getNormalCardPlaysAvailable(P1));

        // Download Gowron. This will force Illegitimate Leader to be discarded.
        performAction(P1, DownloadAction.class, illegitimateLeader);
        assertTrue(selectableCardsAre(P1, downloadablePersonnel));
        selectCard(P1, gowron);
        assertTrue(selectableCardsAre(P1,
                yourAlphaKlingonOutpost, yourDeltaKlingonOutpost, yourNonAlignedOutpost));
        selectCard(P1, yourAlphaKlingonOutpost);
        assertTrue(gowron.isInPlay());
        assertTrue(illegitimateLeader.isInDiscard(_game));

        // Verify normal card play was spent
        assertEquals(0, _game.getGameState().getNormalCardPlaysAvailable(P1));
    }

    @Test
    public void playForFreeTest() throws Exception {

        initializeGame(true);
        assertEquals(1, _game.getGameState().getNormalCardPlaysAvailable(P1));

        // Report Luras for free.
        performAction(P1, ReportCardAction.class, illegitimateLeader);

        if (!selectableCardsAre(P1, reportablePersonnel)) {
            int x = 5;
        }

        assertTrue(selectableCardsAre(P1, reportablePersonnel));
        selectCard(P1, lursa);

        assertTrue(selectableCardsAre(P1, yourAlphaKlingonOutpost, yourDeltaKlingonOutpost, missionWithYourBetor));
        selectCard(P1, yourAlphaKlingonOutpost);

        assertTrue(lursa.isInPlay());
        assertTrue(illegitimateLeader.isInPlay());

        // It didn't use your normal card play, but you can't do it again because of its once each turn limit
        assertEquals(1, _game.getGameState().getNormalCardPlaysAvailable(P1));
        assertThrows(DecisionResultInvalidException.class,
                () -> performAction(P1, ReportCardAction.class, illegitimateLeader));
    }

}