package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_138_034_DrReyga_Test extends AbstractAtTest {

    private PhysicalCard metaphasic;
    private ShipCard runabout1;
    private ShipCard runabout2;
    private PersonnelCard reyga;
    private ShipCard galaxy;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        MissionCard mission2 = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        FacilityCard outpost = builder.addOutpost(Affiliation.FEDERATION, P1, mission); // Federation Outpost
        runabout1 = builder.addShipInSpace("101_331", "Runabout", P1, mission2);
        galaxy = builder.addShipInSpace("101_336", "U.S.S. Galaxy", P1, mission);
        runabout2 = builder.addDockedShip("101_331", "Runabout", P1, outpost);
        ShipCard opposingRunabout = builder.addShipInSpace("101_331", "Runabout", P2, mission);
        metaphasic = builder.addCardInHand("101_083", "Metaphasic Shields", P1);
        reyga = builder.addCardAboardShipOrFacility("138_034", "Dr. Reyga", P1, outpost, PersonnelCard.class);

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
    public void downloadMetaphasicTest() throws Exception {
        initializeGame();
        useGameText(P1, reyga);
        assertTrue(selectableCardsAre(P1, List.of(galaxy, runabout2)));
    }

}