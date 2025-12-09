package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Blueprint_156_010_Surprise_Party_Test extends AbstractAtTest {

    @Test
    public void surprisePartyTest() throws DecisionResultInvalidException, PlayerNotFoundException, InvalidGameOperationException {
        initializeSimple1EGame(40, "156_010"); // Give both players decks full of Surprise Party
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PhysicalCardGroup hand1 = _game.getPlayer(P1).getCardGroup(Zone.HAND);
        PhysicalCardGroup hand2 = _game.getPlayer(P2).getCardGroup(Zone.HAND);

        assertEquals(7, hand1.size());
        assertEquals(7, hand2.size());

        PhysicalCard party = hand1.getFirst();
        assertEquals("Surprise Party", party.getTitle());

        playCard(P1, party);
        assertEquals(6, hand1.size());
        assertEquals(7, hand2.size());
        assertNotNull(_game.getAwaitingDecision(P2));

        chooseOnlyAction(P2);
        assertEquals(9, hand2.size()); // Verify that P2 drew 2 cards

        skipCardPlay();
        skipExecuteOrders();
        assertEquals(7, hand1.size()); // Verify that P1 took their normal card draw

        assertNotNull(_game.getAwaitingDecision(P1)); // Verify that P1 has an optional action
        chooseOnlyAction(P1);
        assertEquals(8, hand1.size()); // Verify that P1 took another card draw
    }

}