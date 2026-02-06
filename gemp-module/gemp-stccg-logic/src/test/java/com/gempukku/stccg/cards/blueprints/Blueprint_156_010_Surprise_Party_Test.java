package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Blueprint_156_010_Surprise_Party_Test extends AbstractAtTest {

    PhysicalCard party;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        party = builder.addCardInHand("156_010", "Surprise Party", P1);
        builder.setPhase(Phase.CARD_PLAY);
        builder.startGame();
    }

    @Test
    public void surprisePartyTest() throws DecisionResultInvalidException, InvalidGameOperationException, CardNotFoundException {
        initializeGame();

        PhysicalCardGroup hand1 = _game.getPlayer(P1).getCardGroup(Zone.HAND);
        PhysicalCardGroup hand2 = _game.getPlayer(P2).getCardGroup(Zone.HAND);

        int startingSize1 = hand1.size();
        int startingSize2 = hand2.size();

        playCard(P1, party);
        assertEquals(startingSize1 - 1, hand1.size());
        assertNotNull(_game.getAwaitingDecision(P2));

        chooseOnlyAction(P2);
        assertEquals(startingSize2 + 2, hand2.size()); // Verify that P2 drew 2 cards

        skipCardPlay();
        skipExecuteOrders();
        assertEquals(startingSize1, hand1.size()); // Verify that P1 took their normal card draw

        assertNotNull(_game.getAwaitingDecision(P1)); // Verify that P1 has an optional action
        chooseOnlyAction(P1);
        assertEquals(startingSize1 + 1, hand1.size()); // Verify that P1 took another card draw
    }

}