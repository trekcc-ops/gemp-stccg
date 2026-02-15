package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_104_FedOutpost_Test extends AbstractAtTest {

    private FacilityCard outpost1;
    private FacilityCard outpost2;
    private MissionCard rogueComet;
    private MissionCard homeworld;
    private MissionCard excavation;
    private MissionCard gammaMission;
    private MissionCard nonFedMission;
    private FacilityCard outpostPlayerTwo;
    private MissionCard noEngineerMission;
    private FacilityCard outpost3;

    private void initializeGame(Zone secondOutpostZone, Phase startingPhase) throws InvalidGameOperationException,
            CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        nonFedMission = builder.addMission("101_157", "Explore Typhon Expanse", P1);
        noEngineerMission = builder.addMission("101_155", "Explore Black Cluster", P1);
        rogueComet = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        excavation = builder.addMission("101_154", "Excavation", P1);
        homeworld = builder.addMission("117_046", "Deliver Message", P1);
        gammaMission = builder.addMission("112_105", "Access Relay Station", P1);
        outpost1 = builder.addSeedDeckCard("101_104", "Federation Outpost", P1, FacilityCard.class);

        if (secondOutpostZone == Zone.SEED_DECK) {
            outpost2 = builder.addSeedDeckCard("101_104", "Federation Outpost", P1, FacilityCard.class);
            outpostPlayerTwo = builder.addSeedDeckCard("101_104", "Federation Outpost", P2, FacilityCard.class);
        } else if (secondOutpostZone == Zone.HAND) {
            outpost2 = builder.addCardInHand("101_104", "Federation Outpost", P1, FacilityCard.class);
            outpost3 = builder.addCardInHand("101_104", "Federation Outpost", P1, FacilityCard.class);
        }

        // Put Fed ENGINEERS at all missions
        for (MissionCard mission : List.of(nonFedMission, rogueComet, excavation, homeworld, gammaMission)) {
            ShipCard runabout = builder.addShipInSpace("101_331", "Runabout", P1, mission);
            builder.addCardAboardShipOrFacility("101_220", "Linda Larson", P1, runabout, PersonnelCard.class);
        }

        builder.setPhase(startingPhase);
        builder.startGame();
    }

    @Test
    public void cannotSeedTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        initializeGame(Zone.SEED_DECK, Phase.SEED_FACILITY);
        seedFacility(P1, outpost1);

        // Verify that the outpost can't be seeded at a non-homeworld, non-Fed mission, or GQ mission
        assertThrows(DecisionResultInvalidException.class, () -> selectCard(P1, nonFedMission));
        assertThrows(DecisionResultInvalidException.class, () -> selectCard(P1, gammaMission));
        assertThrows(DecisionResultInvalidException.class, () -> selectCard(P1, homeworld));

        selectCard(P1, excavation);
        assertTrue(outpost1.isInPlay());
        assertTrue(outpost1.isAtSameLocationAsCard(excavation));

        // Player two can seed the outpost, but player one can't seed two copies
        seedFacility(P2, outpostPlayerTwo);
        selectCard(P2, rogueComet);
        assertThrows(DecisionResultInvalidException.class, () -> seedFacility(P1, outpost2));
    }

    @Test
    public void playMissionTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        initializeGame(Zone.HAND, Phase.SEED_FACILITY);
        seedFacility(P1, outpost1);
        selectCard(P1, excavation);
        assertFalse(outpost2.isInPlay());
        playCard(P1, outpost2);
        CardsSelectionDecision decision = (CardsSelectionDecision) _game.getAwaitingDecision(P1);
        List<? extends PhysicalCard> selectableCards = decision.getSelectableCards();
        assertFalse(selectableCards.contains(noEngineerMission)); // no ENGINEER here
        assertFalse(selectableCards.contains(excavation)); // already an outpost here
        assertFalse(selectableCards.contains(homeworld)); // can't play to a homeworld
        assertTrue(selectableCards.contains(nonFedMission));
        assertTrue(selectableCards.contains(gammaMission));
        assertTrue(selectableCards.contains(rogueComet));
        selectCard(P1, gammaMission);
        assertTrue(outpost2.isAtSameLocationAsCard(gammaMission));

        // Can't play another facility because the normal card play has been used
        assertThrows(DecisionResultInvalidException.class, () -> playCard(P1, outpost3));
    }

}