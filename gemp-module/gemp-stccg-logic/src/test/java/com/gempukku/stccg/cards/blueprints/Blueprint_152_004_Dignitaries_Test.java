package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_152_004_Dignitaries_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard dignitaries;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        dignitaries = builder.addSeedCardUnderMission("152_004", "Dignitaries and Witnesses", P2, _mission);
        ShipCard runabout = builder.addShipInSpace("101_331", "Runabout", P1, _mission);
        builder.addCardAboardShipOrFacility("101_197", "Alynna Nechayev", P1, runabout, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_233", "Sarek", P1, runabout, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, runabout, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void overcomeDilemmaTest() throws Exception {
        // because we have an admiral and INTEGRITY>20
        initializeGame();
        attemptMission(P1, _mission);
        assertEquals(Zone.REMOVED, dignitaries.getZone());
    }

}