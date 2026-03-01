package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_030_ScientificDiplomacy_Test extends AbstractAtTest {

    private MissionCard mission;
    private PhysicalCard sciDiplomacyToSeed;
    private PhysicalCard sciDipInHand1;
    private PhysicalCard sciDipInHand2;
    private PersonnelCard reyga;
    private PersonnelCard christopher;
    private FacilityCard outpost;
    private PersonnelCard amaros;
    private ShipCard mercShip;
    private PhysicalCard taitt1;
    private PhysicalCard taitt2;

    @SuppressWarnings("SpellCheckingInspection")
    public void initializeGame(Phase startingPhase, boolean includePicardOnMercShip)
            throws CardNotFoundException, DecisionResultInvalidException, InvalidGameOperationException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission("155_039", "Host Metaphasic Shielding Test", P1);
        MissionCard mission2 = builder.addMission(MissionType.PLANET, Affiliation.FERENGI, P1);
        outpost = builder.addOutpost(Affiliation.KLINGON, P1, mission);

        builder.addDrawDeckCard("101_083", "Metaphasic Shields", P1);

        // Put some SCIENCE in discard
        builder.addCardInDiscard("112_208", "Jadzia Dax", P1); // DS9 property logo
        taitt1 = builder.addCardInDiscard("101_242", "Taitt", P1);
        taitt2 = builder.addCardInDiscard("101_242", "Taitt", P1);


        mercShip = builder.addShipInSpace("101_354", "Mercenary Ship", P1, mission);
        PersonnelCard picard = (includePicardOnMercShip) ?
                builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, mercShip, PersonnelCard.class) :
                null;
        PersonnelCard baran = builder.addCardAboardShipOrFacility("101_290", "Baran", P1, mercShip, PersonnelCard.class);
        FacilityCard outpost2 = builder.addOutpost(Affiliation.FERENGI, P1, mission2);
        builder.addCardToCoreAsSeeded("155_022", "Continuing Mission", P1); // to get TNG icon
        sciDiplomacyToSeed = builder.addSeedDeckCard("155_030", "Scientific Diplomacy", P1);
        sciDipInHand1 = builder.addCardInHand("155_030", "Scientific Diplomacy", P1);
        sciDipInHand2 = builder.addCardInHand("155_030", "Scientific Diplomacy", P1);
        reyga = builder.addCardInHand("138_034", "Dr. Reyga", P1, PersonnelCard.class);

        // DS9 personnel
        amaros = builder.addCardInHand("112_224", "Amaros", P1, PersonnelCard.class);

        christopher = builder.addCardInHand("155_059", "Dr. Christopher", P1, PersonnelCard.class);
        builder.setPhase(startingPhase);
        builder.startGame();
    }

    @Test
    public void seedCardTest() throws Exception {
        initializeGame(Phase.SEED_FACILITY, false);
        seedCard(P1, sciDiplomacyToSeed);
        assertTrue(sciDiplomacyToSeed.isInPlay());
        assertEquals(mission, sciDiplomacyToSeed.getAtopCard());
        assertFalse(sciDiplomacyToSeed.isOnPlanet(_game));
    }

    @Test
    public void playCardTest() throws Exception {
        initializeGame(Phase.CARD_PLAY, false);
        playCard(P1, sciDipInHand1);
        assertTrue(sciDipInHand1.isInPlay());
        assertEquals(mission, sciDipInHand1.getAtopCard());

        // Can't play the other one because normal card play was used
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P2, sciDipInHand2));

        // You can play a second one on your next turn though
        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        playCard(P1, sciDipInHand2);
        assertTrue(sciDipInHand2.isInPlay());
        assertEquals(mission, sciDipInHand2.getAtopCard());
    }

    @Test
    public void compatibilityAndPlayForFreeTest() throws Exception {
        initializeGame(Phase.CARD_PLAY, true);
        assertFalse(reyga.isCompatibleWith(_game, christopher));
        playCard(P1, sciDipInHand1);
        assertTrue(sciDipInHand1.isInPlay());
        assertTrue(reyga.isCompatibleWith(_game, christopher));
        assertTrue(reyga.isCompatibleWith(_game, outpost));

        // Can play Reyga to the outpost or ship at Veytan for free; can't report to the other outpost because it would require a normal card play
        playCard(P1, reyga);
        assertTrue(selectableCardsAre(P1, List.of(mercShip, outpost)));
        selectCard(P1, outpost);
        assertTrue(reyga.isInPlay());
        assertTrue(reyga.isAboard(outpost));

        // Cannot play Christopher for free because it's a once per turn effect
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, christopher));
    }

    @Test
    public void noTngIconTest() throws Exception {
        initializeGame(Phase.CARD_PLAY, false);
        playCard(P1, sciDipInHand1);
        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        assertTrue(reyga.hasIcon(_game, CardIcon.TNG_ICON));
        playCard(P1, amaros);
        selectCard(P1, outpost);

        // Playing a DS9 card nullifies Continuing Mission; Reyga loses TNG icon
        assertFalse(reyga.hasIcon(_game, CardIcon.TNG_ICON));
        assertFalse(reyga.isCompatibleWith(_game, christopher));

        // Can still play Reyga for free, but only to the Non-Aligned ship
        playCard(P1, reyga);
        assertTrue(reyga.isAboard(mercShip));
    }

    @Test
    public void solveMissionTest() throws Exception {
        initializeGame(Phase.CARD_PLAY, true);
        playCard(P1, sciDipInHand1);
        playCard(P1, reyga);
        selectCard(P1, mercShip);
        playerDecided(P1, ""); // don't special download
        skipPhase(Phase.CARD_PLAY);
        assertEquals(0, _game.getPlayer(P1).getScore());

        attemptMission(P1, mission);
        playerDecided(P1, ""); // don't special download
        assertTrue(mission.isCompleted(_game));
        assertEquals(40, _game.getPlayer(P1).getScore());

        // Can perform 3 actions - download Metaphasic with the mission, use Reyga's DL, or score points with a SCIENCE
        assertEquals(3, getSelectableActionsOfClass(P1, Action.class).size());
        useGameText(P1, sciDipInHand1);
        assertTrue(selectableCardsAre(P1, List.of(taitt1, taitt2)));
        selectCard(P1, taitt1);

        assertEquals(46, _game.getPlayer(P1).getScore());
    }

}