package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoorwaySeedPhaseTest extends AbstractAtTest {

    @Test
    public void doorwayTest() throws DecisionResultInvalidException, InvalidGameOperationException,
            CardNotFoundException {

        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addMissionToDeck("101_154", "Excavation", P1);
        PhysicalCard flash = builder.addSeedDeckCard("105_015", "Q-Flash", P1);
        builder.startGame();

        assertEquals(Phase.SEED_DOORWAY, _game.getCurrentPhase());

        selectCard(P1, flash);

        assertEquals(Phase.SEED_MISSION, _game.getCurrentPhase());
    }

}