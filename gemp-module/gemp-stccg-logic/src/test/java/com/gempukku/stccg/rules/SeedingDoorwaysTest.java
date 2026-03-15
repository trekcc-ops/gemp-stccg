package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SeedingDoorwaysTest extends AbstractAtTest {

    private PhysicalCard doorway;
    private PhysicalCard continuing;

    @Test
    public void facilityPhaseTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        doorway = builder.addSeedDeckCard("103_032", "Alternate Universe Door", P1);
        continuing = builder.addSeedDeckCard("155_022", "Continuing Mission", P1);
        builder.setPhase(Phase.SEED_FACILITY);
        _game = builder.startGame();

        assertThrows(DecisionResultInvalidException.class, () -> seedCard(P1, doorway));
        assertDoesNotThrow(() -> seedCard(P1, continuing));
    }

}