package com.gempukku.stccg.at;

import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CostAtTest extends AbstractAtTest {
    @Test
    public void playOnCostReduction() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, CardDeck> decks = new HashMap<>();

        CardDeck deck = createSimplestDeck();
        decks.put(P1, deck);
        addPlayerDeck(P2, decks, null);

        initializeGameWithDecks(decks);

        PhysicalCardGeneric hobbitSword = createCard(P1, "1_299");

        _game.getGameState().addCardToZone(hobbitSword, Zone.HAND);

        skipMulligans();

        // Play Hobbit Sword (on Frodo) - should be free
        final int twilightPool = _game.getGameState().getTwilightPool();
        playerDecided(P1, "0");
        assertEquals(twilightPool, _game.getGameState().getTwilightPool());
    }

}
