package com.gempukku.stccg.at;

import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AttachAtTest extends AbstractAtTest {
    @Test
    public void extraPossessionClassAttachedTo() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric aragorn = createCard(P1, "1_89");
        PhysicalCardGeneric rangersSword = createCard(P1, "1_112");
        PhysicalCardGeneric knifeOfTheGaladhrim = createCard(P1, "9_17");

        _game.getGameState().addCardToZone(aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(rangersSword, aragorn);
        _game.getGameState().addCardToZone(knifeOfTheGaladhrim, Zone.HAND);

        skipMulligans();

        playerDecided(P1, getCardActionId(P1, "Play Knife"));
        assertEquals(Zone.ATTACHED, knifeOfTheGaladhrim.getZone());
    }
}
