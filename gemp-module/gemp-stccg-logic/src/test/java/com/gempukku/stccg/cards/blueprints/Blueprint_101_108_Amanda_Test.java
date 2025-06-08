package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_108_Amanda_Test extends AbstractAtTest {

    @Test
    public void amandaRogersTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameLogicException, InvalidGameOperationException, PlayerNotFoundException {
        initializeGameToTestMissionAttempt();

        // Figure out which player is going first
        assertEquals(P1, _game.getCurrentPlayerId());

        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) {
            skipDilemma();
        }
        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());

        FacilityCard outpost = null;
        MissionCard excavation = null;
        PersonnelCard worf = (PersonnelCard) newCardForGame("101_251", P1);
        PhysicalCard deathYell = newCardForGame("101_125", P1);
        PhysicalCard amanda = newCardForGame("101_108", P2);

        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Federation Outpost") && card instanceof FacilityCard facility)
                outpost = facility;
            if (Objects.equals(card.getTitle(), "Excavation") && card instanceof MissionCard mission)
                excavation = mission;
        }

        assertNotNull(outpost);
        assertNotNull(excavation);
        assertNotNull(worf);
        assertNotNull(amanda);
        worf.removeFromCardGroup();
        deathYell.removeFromCardGroup();
        amanda.removeFromCardGroup();
        _game.getPlayer(P1).getDrawDeck().addCardToTop(worf);
        _game.getPlayer(P1).getDrawDeck().addCardToTop(deathYell);
        _game.getPlayer(P2).getDrawDeck().addCardToTop(amanda);

        PhysicalCard armus = _game.addCardToGame("101_015", _cardLibrary, P2);
        armus.setZone(Zone.VOID);

        // Seed Armus under Excavation
        MissionLocation kurl = excavation.getLocationDeprecatedOnlyUseForTests();
        seedCardsUnder(Collections.singleton(armus), excavation);

        // Seed Federation Outpost at Excavation
        seedFacility(P1, outpost, excavation.getLocationDeprecatedOnlyUseForTests());
        assertEquals(outpost.getLocationDeprecatedOnlyUseForTests(), excavation.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());
        assertTrue(deathYell.isInHand(_game));

        // Report Worf to outpost
        reportCard(P1, worf, outpost);
        assertTrue(outpost.getCrew().contains(worf));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        // Beam Worf to the planet
        beamCard(P1, outpost, worf, excavation);
        assertTrue(worf.getAwayTeam().isOnSurface(excavation.getLocationDeprecatedOnlyUseForTests()));

        // Attempt mission
        attemptMission(P1, worf.getAwayTeam(), excavation);

        // Confirm that Worf was killed
        assertEquals(Zone.DISCARD, worf.getZone());

        // Play Klingon Death Yell as response
        assertFalse(deathYell.isInPlay());
        selectAction(STCCGPlayCardAction.class, deathYell, P1);

        // P2 plays Amanda Rogers as response
        assertFalse(deathYell.isInPlay());
        selectAction(STCCGPlayCardAction.class, amanda, P2);
        assertTrue(amanda.isInPlay());
        assertTrue(deathYell.isInPlay());
    }

//    @Test
    public void twoAmandasTest() throws DecisionResultInvalidException, CardNotFoundException, InvalidGameLogicException, InvalidGameOperationException, PlayerNotFoundException {
        initializeGameToTestMissionAttempt();

        // Figure out which player is going first
        assertEquals(P1, _game.getCurrentPlayerId());

        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) {
            skipDilemma();
        }
        assertEquals(Phase.SEED_FACILITY, _game.getCurrentPhase());

        FacilityCard outpost = null;
        MissionCard excavation = null;
        PersonnelCard worf = (PersonnelCard) newCardForGame("101_251", P1);
        PhysicalCard deathYell = newCardForGame("101_125", P1);
        PhysicalCard amanda1 = newCardForGame("101_108", P1);
        PhysicalCard amanda2 = newCardForGame("101_108", P2);

        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Federation Outpost") && card instanceof FacilityCard facility)
                outpost = facility;
            if (Objects.equals(card.getTitle(), "Excavation") && card instanceof MissionCard mission)
                excavation = mission;
        }

        assertNotNull(outpost);
        assertNotNull(excavation);
        assertNotNull(worf);
        assertNotNull(amanda1);
        assertNotNull(amanda2);
        worf.removeFromCardGroup();
        deathYell.removeFromCardGroup();
        amanda1.removeFromCardGroup();
        amanda2.removeFromCardGroup();
        _game.getPlayer(P1).getDrawDeck().addCardToTop(worf);
        _game.getPlayer(P1).getDrawDeck().addCardToTop(deathYell);
        _game.getPlayer(P1).getDrawDeck().addCardToTop(amanda1);
        _game.getPlayer(P2).getDrawDeck().addCardToTop(amanda2);

        PhysicalCard armus = _game.addCardToGame("101_015", _cardLibrary, P2);
        armus.setZone(Zone.VOID);

        // Seed Armus under Excavation
        MissionLocation kurl = excavation.getLocationDeprecatedOnlyUseForTests();
        seedCardsUnder(Collections.singleton(armus), excavation);

        // Seed Federation Outpost at Excavation
        seedFacility(P1, outpost, excavation.getLocationDeprecatedOnlyUseForTests());
        assertEquals(outpost.getLocationDeprecatedOnlyUseForTests(), excavation.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());
        assertTrue(deathYell.isInHand(_game));

        // Report Worf to outpost
        reportCard(P1, worf, outpost);
        assertTrue(outpost.getCrew().contains(worf));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        // Beam Worf to the planet
        beamCard(P1, outpost, worf, excavation);
        assertTrue(worf.getAwayTeam().isOnSurface(excavation.getLocationDeprecatedOnlyUseForTests()));

        // Attempt mission
        attemptMission(P1, worf.getAwayTeam(), excavation);

        // Confirm that Worf was killed
        assertEquals(Zone.DISCARD, worf.getZone());

        // Play Klingon Death Yell as response
        assertFalse(deathYell.isInPlay());
        selectAction(STCCGPlayCardAction.class, deathYell, P1);

        // P2 plays Amanda Rogers as response
        assertFalse(deathYell.isInPlay());
        selectAction(STCCGPlayCardAction.class, amanda2, P2);
        assertTrue(amanda2.isInPlay());
        assertTrue(deathYell.isInPlay());
    }

}