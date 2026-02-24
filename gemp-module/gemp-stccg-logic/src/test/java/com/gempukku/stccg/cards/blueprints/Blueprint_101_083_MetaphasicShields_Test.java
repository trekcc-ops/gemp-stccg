package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_101_083_MetaphasicShields_Test extends AbstractAtTest {

    private PhysicalCard metaphasic;
    private ShipCard runabout1;
    private ShipCard runabout2;
    private PhysicalCard metaphasic2;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        FacilityCard outpost = builder.addOutpost(Affiliation.FEDERATION, P1); // Federation Outpost
        runabout1 = builder.addShipInSpace("101_331", "Runabout", P1);
        runabout2 = builder.addDockedShip("101_331", "Runabout", P1, outpost);
        ShipCard opposingRunabout = builder.addShipInSpace("101_331", "Runabout", P2);
        metaphasic = builder.addCardInHand("101_083", "Metaphasic Shields", P1);
        metaphasic2 = builder.addCardInHand("101_083", "Metaphasic Shields", P1);

        // your SCIENCE skill on runabout 1
        builder.addCardAboardShipOrFacility("116_072", "Sarita Carson", P1, runabout1, PersonnelCard.class);

        // your SCIENCE classification on runabout 1
        builder.addCardAboardShipOrFacility("101_223", "Mendon", P1, runabout1, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_223", "Mendon", P1, runabout1, PersonnelCard.class);
        
        // opponent's SCIENCE classification on runabout 1
        builder.addCardAboardShipOrFacility("101_223", "Mendon", P2, runabout1, PersonnelCard.class);

        // your SCIENCE classification on runabout 2
        builder.addCardAboardShipOrFacility("101_223", "Mendon", P1, runabout2, PersonnelCard.class);

        // your SCIENCE classification on outpost
        builder.addCardAboardShipOrFacility("101_223", "Mendon", P1, outpost, PersonnelCard.class);

        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void playCardAndCheckAttributesTest() throws Exception {
        initializeGame();
        assertEquals(5, runabout1.getShields(_game));
        playCard(P1, metaphasic);
        assertTrue(selectableCardsAre(P1, List.of(runabout1, runabout2)));
        selectCard(P1, runabout1);
        assertEquals(9, runabout1.getShields(_game));
    }

    @Test
    public void noCumulativeTest() throws Exception {
        initializeGame();
        assertEquals(5, runabout1.getShields(_game));
        playCard(P1, metaphasic);
        selectCard(P1, runabout1);
        assertEquals(9, runabout1.getShields(_game));

        skipToNextTurnAndPhase(P1, Phase.CARD_PLAY);
        playCard(P1, metaphasic2);
        selectCard(P1, runabout1);
        assertEquals(runabout1, metaphasic.getParentCard());
        assertEquals(runabout1, metaphasic2.getParentCard());
        assertEquals(9, runabout1.getShields(_game));
    }

    @Test
    public void noBenefitFromPersonnelAboardFacilityTest() throws Exception {
        initializeGame();
        assertEquals(20, runabout2.getShields(_game));
        playCard(P1, metaphasic);
        assertTrue(selectableCardsAre(P1, List.of(runabout1, runabout2)));
        selectCard(P1, runabout2);
        assertEquals(22, runabout2.getShields(_game));
    }


}