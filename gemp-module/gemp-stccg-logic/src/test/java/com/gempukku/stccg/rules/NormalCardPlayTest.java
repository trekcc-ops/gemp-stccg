package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NormalCardPlayTest extends AbstractAtTest {

    @Test
    public void normalCardPlayTest()
            throws DecisionResultInvalidException, InvalidGameLogicException, PlayerNotFoundException, InvalidGameOperationException {
        initializeQuickMissionAttempt("Excavation");

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(), _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());
        Player player1 = _game.getPlayer(P1);

        PhysicalCard wallace = player1.getCardsInHand().get(1);
        assertEquals("Darian Wallace", wallace.getTitle());
        reportCard(P1, wallace, _outpost);
        assertTrue(_outpost.getCrew().contains(wallace));

        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());
    }

}