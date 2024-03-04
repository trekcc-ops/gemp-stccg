package com.gempukku.stccg.at;

import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class CultureTokenAtTest extends AbstractAtTest {
    @Test
    public void removeCultureTokenWithoutOne() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric sauronsMight = createCard(P2, "19_27");

        _game.getGameState().addCardToZone(sauronsMight, Zone.HAND);

        skipMulligans();

        playerDecided(P1, "");
        assertNull(getCardActionId(P2, "Play Sauron's Might"));
    }
}
