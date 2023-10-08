package com.gempukku.stccg.at;

import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.PhysicalCardImpl;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class CultureTokenAtTest extends AbstractAtTest {
    @Test
    public void removeCultureTokenWithoutOne() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl sauronsMight = createCard(P2, "19_27");

        _game.getGameState().addCardToZone(_game, sauronsMight, Zone.HAND);

        skipMulligans();

        playerDecided(P1, "");
        assertNull(getCardActionId(P2, "Play Sauron's Might"));
    }
}
