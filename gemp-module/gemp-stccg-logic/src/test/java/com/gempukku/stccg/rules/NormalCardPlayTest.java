package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NormalCardPlayTest extends AbstractAtTest {

    @Test
    public void normalCardPlayTest()
            throws DecisionResultInvalidException, InvalidGameLogicException, PlayerNotFoundException, InvalidGameOperationException {
        initializeQuickMissionAttempt("Excavation");

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(), _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());
        Player player1 = _game.getPlayer(P1);

        PhysicalCard tarses1 = player1.getCardsInHand().get(1);
        assertEquals("Simon Tarses", tarses1.getTitle());
        reportCard(P1, tarses1, _outpost);
        assertTrue(_outpost.getCrew().contains(tarses1));

        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
    }

}