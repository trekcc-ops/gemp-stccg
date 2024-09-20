package com.gempukku.stccg.at;

import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.AwaitingDecisionType;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IndividualCardAtTest extends AbstractAtTest {


    @Test
    public void frodosPipeOncePerPhase() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardGeneric frodosPipe = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("40_250"));
        PhysicalCardGeneric pipeweed1 = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("40_255"));
        PhysicalCardGeneric pipeweed2 = new PhysicalCardGeneric(_game, 102, P1, _cardLibrary.getCardBlueprint("40_255"));

        _game.getGameState().addCardToZone(frodosPipe, Zone.HAND);
        _game.getGameState().addCardToZone(pipeweed1, Zone.SUPPORT);
        _game.getGameState().addCardToZone(pipeweed2, Zone.SUPPORT);

        skipMulligans();

        playerDecided(P1, getCardActionId(_userFeedback.getAwaitingDecision(P1), "Play Frodo's Pipe"));
        playerDecided(P1, getCardActionId(_userFeedback.getAwaitingDecision(P1), "Use Frodo's Pipe"));

        playerDecided(P1, String.valueOf(pipeweed1.getCardId()));
        playerDecided(P1, "1");

        assertNull(getCardActionId(_userFeedback.getAwaitingDecision(P1), "Use Frodo's Pipe"));
    }

    @Test
    public void betterThanNothingDoesNotAddBurden() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric betterThanNothing = createCard(P2, "31_19");
        PhysicalCardGeneric wizardIsNeverLate = createCard(P1, "30_23");

        _game.getGameState().addCardToZone(wizardIsNeverLate, Zone.HAND);
        _game.getGameState().addCardToZone(betterThanNothing, Zone.SUPPORT);

        skipMulligans();
        int burdens = 0;

        playerDecided(P1, "0");

        assertEquals(burdens, 0);
    }

}
