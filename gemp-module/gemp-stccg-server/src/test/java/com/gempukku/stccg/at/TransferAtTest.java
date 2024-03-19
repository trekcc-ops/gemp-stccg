package com.gempukku.stccg.at;

import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TransferAtTest extends AbstractAtTest {
    @Test
    public void transfer() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardGeneric athelas= new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("1_94"));
        PhysicalCardGeneric aragorn= new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_89"));
        PhysicalCardGeneric boromir= new PhysicalCardGeneric(_game, 102, P1, _cardLibrary.getCardBlueprint("1_96"));

        _game.getGameState().addCardToZone(aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(boromir, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(athelas, Zone.ATTACHED);
        athelas.attachTo(aragorn);

        skipMulligans();

        final String transferAction = getCardActionId(_userFeedback.getAwaitingDecision(P1), "Transfer");
        playerDecided(P1, transferAction);

        assertEquals(boromir, athelas.getAttachedTo());
    }
}
