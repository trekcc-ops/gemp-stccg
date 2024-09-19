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
    public void mumakChieftainPlayingMumakForFree() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardGeneric mumakChieftain = new PhysicalCardGeneric(_game, 100, P2, _cardLibrary.getCardBlueprint("10_45"));
        PhysicalCardGeneric mumak = new PhysicalCardGeneric(_game, 100, P2, _cardLibrary.getCardBlueprint("5_73"));

        skipMulligans();

        _game.getGameState().addCardToZone(mumak, Zone.DISCARD);
        _game.getGameState().addCardToZone(mumakChieftain, Zone.HAND);
        _game.getGameState().setTwilight(5);

        // End fellowship phase
        playerDecided(P1, "");

        assertEquals(7, _game.getGameState().getTwilightPool());

        // Play mumak chieftain
        final AwaitingDecision shadowDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, shadowDecision.getDecisionType());
        validateContents(new String[]{String.valueOf(mumakChieftain.getCardId())}, shadowDecision.getDecisionParameters().get("cardId"));
        playerDecided(P2, "0");

        assertEquals(0, _game.getGameState().getTwilightPool());

        final AwaitingDecision optionalPlayDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalPlayDecision.getDecisionType());
        validateContents(new String[]{String.valueOf(mumakChieftain.getCardId())}, optionalPlayDecision.getDecisionParameters().get("cardId"));
        playerDecided(P2, "0");

        assertEquals(Zone.ATTACHED, mumak.getZone());
    }

    @Test
    public void orcMarksmanUnique() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardGeneric marksman1 = new PhysicalCardGeneric(_game, 100, P2, _cardLibrary.getCardBlueprint("40_227"));
        PhysicalCardGeneric marksman2 = new PhysicalCardGeneric(_game, 101, P2, _cardLibrary.getCardBlueprint("40_227"));

        skipMulligans();

        _game.getGameState().addCardToZone(marksman1, Zone.HAND);
        _game.getGameState().addCardToZone(marksman2, Zone.HAND);

        _game.getGameState().addTwilight(10);

        // End fellowship
        playerDecided(P1, "");

        playerDecided(P2, "0");
        assertNull(getCardActionId(_userFeedback.getAwaitingDecision(P2), "Play Orc Mark"));
    }

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
