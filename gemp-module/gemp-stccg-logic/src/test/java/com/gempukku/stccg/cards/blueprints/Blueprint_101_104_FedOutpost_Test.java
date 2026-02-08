package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_104_FedOutpost_Test extends AbstractAtTest {

    private FacilityCard outpost1;
    private FacilityCard outpost2;
    private MissionCard rogueComet;
    private MissionCard excavation;

    private void initializeGame(Zone secondOutpostZone, Phase startingPhase, boolean includeEngineer) throws InvalidGameOperationException,
            CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        rogueComet = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        excavation = builder.addMission("101_154", "Excavation", P1);
        outpost1 = builder.addSeedDeckCard("101_104", "Federation Outpost", P1, FacilityCard.class);

        if (secondOutpostZone == Zone.SEED_DECK) {
            outpost2 = builder.addSeedDeckCard("101_104", "Federation Outpost", P1, FacilityCard.class);
        } else if (secondOutpostZone == Zone.HAND) {
            outpost2 = builder.addCardInHand("101_104", "Federation Outpost", P1, FacilityCard.class);
        }

        ShipCard runabout = builder.addShipInSpace("101_331", "Runabout", P1, rogueComet);
        if (includeEngineer) {
            PersonnelCard geordi = builder.addCardAboardShipOrFacility("101_212", "Geordi La Forge", P1, runabout, PersonnelCard.class);
        }
        builder.setPhase(startingPhase);
        builder.startGame();
    }

    @Test
    public void canOnlySeedOneTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        initializeGame(Zone.SEED_DECK, Phase.SEED_FACILITY, false);
        seedFacility(P1, outpost1);
        selectCard(P1, excavation);
        assertTrue(outpost1.isInPlay());
        assertTrue(outpost1.isAtSameLocationAsCard(excavation));
        assertThrows(DecisionResultInvalidException.class, () -> seedFacility(P1, outpost2));
    }

    @Test
    public void isUniversalTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        initializeGame(Zone.HAND, Phase.SEED_FACILITY, true);
        seedFacility(P1, outpost1);
        selectCard(P1, excavation);
        assertFalse(outpost2.isInPlay());
        playFacility(P1, outpost2);
        assertTrue(outpost2.isInPlay());
    }

    @Test
    public void failToPlayTest() throws DecisionResultInvalidException, CardNotFoundException,
            InvalidGameOperationException {
        initializeGame(Zone.HAND, Phase.CARD_PLAY, false);
        boolean errorFlagged = false;
        try {
            playFacility(P1, outpost2);
        } catch(DecisionResultInvalidException exp) {
            errorFlagged = true;
        }
        assertTrue(errorFlagged);
        assertFalse(outpost2.isInPlay());
    }

}