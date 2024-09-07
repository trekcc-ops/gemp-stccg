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
    public void oneGoodTurnDeservesAnother() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric smeagol = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("5_29"));
        PhysicalCardGeneric oneGoodTurnDeservesAnother = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("11_49"));

        _game.getGameState().addCardToZone(oneGoodTurnDeservesAnother, Zone.HAND);
        _game.getGameState().addCardToZone(smeagol, Zone.FREE_CHARACTERS);

        skipMulligans();

        playerDecided(P1, "0");

        assertEquals(1, 0);
        assertEquals(0, _game.getGameState().getHand(P1).size());

        playerDecided(P1, "0");

        assertEquals(2, 0);
        assertEquals(1, _game.getGameState().getHand(P1).size());
    }

    @Test
    public void sentBackAllowsPlayingCardInDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric sentBack = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("9_27"));
        _game.getGameState().addCardToZone(sentBack, Zone.SUPPORT);

        PhysicalCardGeneric radagast1 = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("9_26"));
        _game.getGameState().addCardToZone(radagast1, Zone.DEAD);

        PhysicalCardGeneric radagast2 = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("9_26"));
        _game.getGameState().addCardToZone(radagast2, Zone.HAND);

        skipMulligans();

        // End fellowship
        AwaitingDecision playFellowshipAction = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playFellowshipAction.getDecisionType());
        validateContents(new String[]{String.valueOf(sentBack.getCardId())}, playFellowshipAction.getDecisionParameters().get("cardId"));
        playerDecided(P1, "0");

        assertEquals(Zone.FREE_CHARACTERS, radagast2.getZone());
        assertEquals(4, _game.getGameState().getTwilightPool());
    }

    @Test
    public void hisFirstSeriousCheck() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric gandalf = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("1_72"));
        _game.getGameState().addCardToZone(gandalf, Zone.FREE_CHARACTERS);

        PhysicalCardGeneric hisFirstSeriousCheck = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("3_33"));
        _game.getGameState().addCardToZone(hisFirstSeriousCheck, Zone.HAND);

        skipMulligans();

        // End fellowship
        playerDecided(P1, "");

        PhysicalCardGeneric urukHaiRaidingParty = new PhysicalCardGeneric(_game, 102, P2, _cardLibrary.getCardBlueprint("1_158"));
        _game.getGameState().addCardToZone(urukHaiRaidingParty, Zone.SHADOW_CHARACTERS);

        // End shadow
        playerDecided(P2, "");

        AwaitingDecision playManeuverAction = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playManeuverAction.getDecisionType());
        validateContents(new String[]{String.valueOf(hisFirstSeriousCheck.getCardId())}, playManeuverAction.getDecisionParameters().get("cardId"));
    }

    @Test
    public void moreYetToComeWorks() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric gimli = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("1_12"));
        PhysicalCardGeneric moreYetToCome = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("10_3"));
        PhysicalCardGeneric goblinRunner = new PhysicalCardGeneric(_game, 102, P2, _cardLibrary.getCardBlueprint("1_178"));
        PhysicalCardGeneric goblinRunner2 = new PhysicalCardGeneric(_game, 103, P2, _cardLibrary.getCardBlueprint("1_178"));

        _game.getGameState().addCardToZone(moreYetToCome, Zone.HAND);

        skipMulligans();

        _game.getGameState().addCardToZone(gimli, Zone.FREE_CHARACTERS);

        // End fellowship
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(goblinRunner, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(goblinRunner2, Zone.SHADOW_CHARACTERS);

        // End shadow
        playerDecided(P2, "");

        // End maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End archery
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign Gimli to goblin runner
        playerDecided(P1, gimli.getCardId() + " " + goblinRunner.getCardId());

        playerDecided(P2, "");

        // Choose skirmish to start
        playerDecided(P1, String.valueOf(gimli.getCardId()));

        // End skirmish
        playerDecided(P1, "");
        playerDecided(P2, "");

        assertEquals(Zone.DISCARD, goblinRunner.getZone());
        AwaitingDecision playMoreYetToCome = _userFeedback.getAwaitingDecision(P1);
        playerDecided(P1, getCardActionId(playMoreYetToCome, "Play More"));

        assertEquals(Zone.DISCARD, goblinRunner2.getZone());
    }

    @Test
    public void treebeardEarthborn() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardGeneric treebeard = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("4_103"));
        PhysicalCardGeneric merry = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("4_311"));
        PhysicalCardGeneric goblinRunner = new PhysicalCardGeneric(_game, 102, P2, _cardLibrary.getCardBlueprint("1_178"));

        skipMulligans();

        _game.getGameState().addCardToZone(treebeard, Zone.SUPPORT);
        _game.getGameState().addCardToZone(merry, Zone.FREE_CHARACTERS);

        // End fellowship
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(goblinRunner, Zone.SHADOW_CHARACTERS);

        // End shadow
        playerDecided(P2, "");

        // End maneuver
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End archery
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End assignment
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Assign Gimli to goblin runner
        PhysicalCard frodo = null;
        playerDecided(P1, frodo.getCardId() + " " + goblinRunner.getCardId());

        // Choose skirmish to start
        playerDecided(P1, String.valueOf(frodo.getCardId()));

        AwaitingDecision playSkirmishAction = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playSkirmishAction.getDecisionType());
        playerDecided(P1, getCardActionId(playSkirmishAction, "Use Merry"));

        AwaitingDecision treebeardDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, treebeardDecision.getDecisionType());
        playerDecided(P1, getCardActionId(treebeardDecision, "Use Tree"));

        assertEquals(Zone.STACKED, merry.getZone());
        assertEquals(treebeard, merry.getStackedOn());
    }

    @Test
    public void trollMonstrousFiend() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardGeneric troll = createCard(P2, "40_157");
        PhysicalCardGeneric runner1 = createCard(P2, "40_169");
        PhysicalCardGeneric runner2 = createCard(P2, "40_169");
        PhysicalCardGeneric runner3 = createCard(P2, "40_169");
        PhysicalCardGeneric runner4 = createCard(P2, "40_169");

        skipMulligans();

        _game.getGameState().addCardToZone(troll, Zone.HAND);
        _game.getGameState().addCardToZone(runner1, Zone.HAND);
        _game.getGameState().addCardToZone(runner2, Zone.HAND);
        _game.getGameState().addCardToZone(runner3, Zone.HAND);
        _game.getGameState().addCardToZone(runner4, Zone.HAND);

        _game.getGameState().addTwilight(7);

        // End fellowship
        playerDecided(P1, "");

        assertEquals(9, _game.getGameState().getTwilightPool());

        final AwaitingDecision playShadowAction = _userFeedback.getAwaitingDecision(P2);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playShadowAction.getDecisionType());
        playerDecided(P2, getCardActionId(playShadowAction, "Play Cave"));

        final AwaitingDecision discardGoblins = _userFeedback.getAwaitingDecision(P2);
        assertEquals("3", discardGoblins.getDecisionParameters().get("min")[0]);
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
    public void athelasDoesNothing() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardGeneric athelas = new PhysicalCardGeneric(_game, 100, P1, _cardLibrary.getCardBlueprint("1_94"));
        PhysicalCardGeneric aragorn = new PhysicalCardGeneric(_game, 101, P1, _cardLibrary.getCardBlueprint("1_89"));

        _game.getGameState().addCardToZone(aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(athelas, aragorn);

        skipMulligans();

        // Use Athelas
        playerDecided(P1, getCardActionId(P1, "Use Athelas"));

        assertEquals(Zone.DISCARD, athelas.getZone());

        // Pass
        playerDecided(P1, "");

        playerDecided(P2, "");
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
