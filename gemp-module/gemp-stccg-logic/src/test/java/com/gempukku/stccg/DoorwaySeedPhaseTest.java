package com.gempukku.stccg;

import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoorwaySeedPhaseTest extends AbstractAtTest {

    @Test
    public void doorwayTest() throws DecisionResultInvalidException, PlayerNotFoundException, InvalidGameOperationException {
        initializeSimple1EGameWithDoorways(30);
        assertEquals(Phase.SEED_DOORWAY, _game.getCurrentPhase());
        Player player1 = _game.getPlayer(P1);
        Player player2 = _game.getPlayer(P2);

        selectCard(P1, player1.getCardsInGroup(Zone.SEED_DECK).getFirst());
        selectCard(P2, player2.getCardsInGroup(Zone.SEED_DECK).getFirst());

        assertEquals(Phase.SEED_MISSION, _game.getCurrentPhase());
    }

}