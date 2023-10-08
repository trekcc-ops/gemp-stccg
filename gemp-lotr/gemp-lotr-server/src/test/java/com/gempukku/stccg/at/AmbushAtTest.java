package com.gempukku.stccg.at;

import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.PhysicalCardImpl;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.AwaitingDecisionType;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AmbushAtTest extends AbstractAtTest {
    @Test
    public void cantPlayIfNotEnoughAndCantExertAnything() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl gimli = new PhysicalCardImpl(100, "5_7", P1, _cardLibrary.getLotroCardBlueprint("5_7"));
        PhysicalCardImpl desertLegion = new PhysicalCardImpl(101, "4_218", P2, _cardLibrary.getLotroCardBlueprint("4_218"));

        skipMulligans();

        // Fellowship phase
        _game.getGameState().addCardToZone(_game, gimli, Zone.FREE_CHARACTERS);
        playerDecided(P1, "");

        // Shadow phase
        _game.getGameState().addCardToZone(_game, desertLegion, Zone.SHADOW_CHARACTERS);
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
        playerDecided(P1, gimli.getCardId() + " " + desertLegion.getCardId());

        // FP player gets no Ambush trigger
        assertNull(_userFeedback.getAwaitingDecision(P1));

        // Shadow player gets an Ambush trigger
        AwaitingDecision ambushTriggerDecision = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, ambushTriggerDecision.getDecisionType());
        validateContents(new String[]{String.valueOf(desertLegion.getCardId())}, ambushTriggerDecision.getDecisionParameters().get("cardId"));
        assertTrue(((String[]) ambushTriggerDecision.getDecisionParameters().get("actionText"))[0].startsWith("Ambush "));

        assertEquals(3, _game.getGameState().getTwilightPool());

        playerDecided(P2, getCardActionId(ambushTriggerDecision, "Ambush "));

        assertEquals(4, _game.getGameState().getTwilightPool());
    }
}
