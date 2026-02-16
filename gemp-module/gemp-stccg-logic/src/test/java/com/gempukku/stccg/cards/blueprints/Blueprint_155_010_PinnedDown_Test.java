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

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_155_010_PinnedDown_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard pinnedDown;
    private ShipCard runabout;
    private Collection<PersonnelCard> attemptingPersonnel;

    private void initializeGame(int personnelToAttempt) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        pinnedDown = builder.addSeedCardUnderMission("155_010", "Pinned Down", P2, _mission);
        runabout = builder.addShipInSpace("101_331", "Runabout", P1, _mission);
        attemptingPersonnel = new ArrayList<>();
        for (int i = 0; i < personnelToAttempt; i++) {
            PersonnelCard larson = builder.addCardAboardShipOrFacility(
                    "101_220", "Linda Larson", P1, runabout, PersonnelCard.class);
            attemptingPersonnel.add(larson);
        }
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void failDilemmaTest() throws Exception {
        initializeGame(1);
        assertEquals(1, attemptingPersonnel.size());
        attemptMission(P1, _mission);
        assertNotEquals(Zone.REMOVED, pinnedDown.getZone());
    }

}