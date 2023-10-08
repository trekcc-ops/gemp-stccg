package com.gempukku.stccg.at;

import com.gempukku.stccg.cards.PhysicalCardImpl;
import com.gempukku.stccg.common.filterable.Token;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExertAtTest extends AbstractAtTest{
    @Test
    public void glorfindelCantExertWhenExhausted() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl glorfindel = createCard(P1, "9_16");
        PhysicalCardImpl elvenBow = createCard(P1, "1_41");

        PhysicalCardImpl cantea = createCard(P2, "1_230");

        skipMulligans();

        moveCardToZone(glorfindel, Zone.FREE_CHARACTERS);
        moveCardToZone(cantea, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addTokens(glorfindel, Token.WOUND, 2);
        _game.getGameState().putCardOnTopOfDeck(elvenBow);

        // End Fellowship phase
        playerDecided(P1, "");
        // End Shadow phase
        playerDecided(P2, "");

        // End maneuver phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End archery phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign
        playerDecided(P1, glorfindel.getCardId() + " " + cantea.getCardId());
        // Start skirmish
        playerDecided(P1, String.valueOf(glorfindel.getCardId()));

        // Use Glorfindel
        playerDecided(P1, getCardActionId(P1, "Use Glorfindel"));
        // Pass on viewing revealed card
        playerDecided(P1, "");
        // Pass on viewing revealed card
        playerDecided(P2, "");
        // Can't exert (already exhausted)
        playerDecided(P1, "0");
        assertEquals(10, _game.getModifiersQuerying().getStrength(_game, cantea));
    }
}
