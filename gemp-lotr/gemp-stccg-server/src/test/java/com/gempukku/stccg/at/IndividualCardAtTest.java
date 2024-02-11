package com.gempukku.stccg.at;

import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalCardImpl;
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

        PhysicalCardImpl mumakChieftain = new PhysicalCardImpl(_game, 100, "10_45", P2, _cardLibrary.getCardBlueprint("10_45"));
        PhysicalCardImpl mumak = new PhysicalCardImpl(_game, 100, "5_73", P2, _cardLibrary.getCardBlueprint("5_73"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, mumak, Zone.DISCARD);
        _game.getGameState().addCardToZone(_game, mumakChieftain, Zone.HAND);
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

        PhysicalCardImpl smeagol = new PhysicalCardImpl(_game, 100, "5_29", P1, _cardLibrary.getCardBlueprint("5_29"));
        PhysicalCardImpl oneGoodTurnDeservesAnother = new PhysicalCardImpl(_game, 101, "11_49", P1, _cardLibrary.getCardBlueprint("11_49"));

        _game.getGameState().addCardToZone(_game, oneGoodTurnDeservesAnother, Zone.HAND);
        _game.getGameState().addCardToZone(_game, smeagol, Zone.FREE_CHARACTERS);

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

        PhysicalCardImpl sentBack = new PhysicalCardImpl(_game, 100, "9_27", P1, _cardLibrary.getCardBlueprint("9_27"));
        _game.getGameState().addCardToZone(_game, sentBack, Zone.SUPPORT);

        PhysicalCardImpl radagast1 = new PhysicalCardImpl(_game, 101, "9_26", P1, _cardLibrary.getCardBlueprint("9_26"));
        _game.getGameState().addCardToZone(_game, radagast1, Zone.DEAD);

        PhysicalCardImpl radagast2 = new PhysicalCardImpl(_game, 101, "9_26", P1, _cardLibrary.getCardBlueprint("9_26"));
        _game.getGameState().addCardToZone(_game, radagast2, Zone.HAND);

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

        PhysicalCardImpl gandalf = new PhysicalCardImpl(_game, 100, "1_72", P1, _cardLibrary.getCardBlueprint("1_72"));
        _game.getGameState().addCardToZone(_game, gandalf, Zone.FREE_CHARACTERS);

        PhysicalCardImpl hisFirstSeriousCheck = new PhysicalCardImpl(_game, 100, "3_33", P1, _cardLibrary.getCardBlueprint("3_33"));
        _game.getGameState().addCardToZone(_game, hisFirstSeriousCheck, Zone.HAND);

        skipMulligans();

        // End fellowship
        playerDecided(P1, "");

        PhysicalCardImpl urukHaiRaidingParty = new PhysicalCardImpl(_game, 102, "1_158", P2, _cardLibrary.getCardBlueprint("1_158"));
        _game.getGameState().addCardToZone(_game, urukHaiRaidingParty, Zone.SHADOW_CHARACTERS);

        // End shadow
        playerDecided(P2, "");

        AwaitingDecision playManeuverAction = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, playManeuverAction.getDecisionType());
        validateContents(new String[]{String.valueOf(hisFirstSeriousCheck.getCardId())}, playManeuverAction.getDecisionParameters().get("cardId"));
    }

    @Test
    public void moreYetToComeWorks() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl gimli = new PhysicalCardImpl(_game, 100, "1_12", P1, _cardLibrary.getCardBlueprint("1_12"));
        PhysicalCardImpl moreYetToCome = new PhysicalCardImpl(_game, 101, "10_3", P1, _cardLibrary.getCardBlueprint("10_3"));
        PhysicalCardImpl goblinRunner = new PhysicalCardImpl(_game, 102, "1_178", P2, _cardLibrary.getCardBlueprint("1_178"));
        PhysicalCardImpl goblinRunner2 = new PhysicalCardImpl(_game, 103, "1_178", P2, _cardLibrary.getCardBlueprint("1_178"));

        _game.getGameState().addCardToZone(_game, moreYetToCome, Zone.HAND);

        skipMulligans();

        _game.getGameState().addCardToZone(_game, gimli, Zone.FREE_CHARACTERS);

        // End fellowship
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, goblinRunner2, Zone.SHADOW_CHARACTERS);

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

        PhysicalCardImpl treebeard = new PhysicalCardImpl(_game, 100, "4_103", P1, _cardLibrary.getCardBlueprint("4_103"));
        PhysicalCardImpl merry = new PhysicalCardImpl(_game, 101, "4_311", P1, _cardLibrary.getCardBlueprint("4_311"));
        PhysicalCardImpl goblinRunner = new PhysicalCardImpl(_game, 102, "1_178", P2, _cardLibrary.getCardBlueprint("1_178"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, treebeard, Zone.SUPPORT);
        _game.getGameState().addCardToZone(_game, merry, Zone.FREE_CHARACTERS);

        // End fellowship
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.SHADOW_CHARACTERS);

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

        PhysicalCardImpl troll = createCard(P2, "40_157");
        PhysicalCardImpl runner1 = createCard(P2, "40_169");
        PhysicalCardImpl runner2 = createCard(P2, "40_169");
        PhysicalCardImpl runner3 = createCard(P2, "40_169");
        PhysicalCardImpl runner4 = createCard(P2, "40_169");

        skipMulligans();

        _game.getGameState().addCardToZone(_game, troll, Zone.HAND);
        _game.getGameState().addCardToZone(_game, runner1, Zone.HAND);
        _game.getGameState().addCardToZone(_game, runner2, Zone.HAND);
        _game.getGameState().addCardToZone(_game, runner3, Zone.HAND);
        _game.getGameState().addCardToZone(_game, runner4, Zone.HAND);

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

        PhysicalCardImpl marksman1 = new PhysicalCardImpl(_game, 100, "40_227", P2, _cardLibrary.getCardBlueprint("40_227"));
        PhysicalCardImpl marksman2 = new PhysicalCardImpl(_game, 101, "40_227", P2, _cardLibrary.getCardBlueprint("40_227"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, marksman1, Zone.HAND);
        _game.getGameState().addCardToZone(_game, marksman2, Zone.HAND);

        _game.getGameState().addTwilight(10);

        // End fellowship
        playerDecided(P1, "");

        playerDecided(P2, "0");
        assertNull(getCardActionId(_userFeedback.getAwaitingDecision(P2), "Play Orc Mark"));
    }

    @Test
    public void frodosPipeOncePerPhase() throws CardNotFoundException, DecisionResultInvalidException {
        initializeSimplestGame();

        PhysicalCardImpl frodosPipe = new PhysicalCardImpl(_game, 100, "40_250", P1, _cardLibrary.getCardBlueprint("40_250"));
        PhysicalCardImpl pipeweed1 = new PhysicalCardImpl(_game, 101, "40_255", P1, _cardLibrary.getCardBlueprint("40_255"));
        PhysicalCardImpl pipeweed2 = new PhysicalCardImpl(_game, 102, "40_255", P1, _cardLibrary.getCardBlueprint("40_255"));

        _game.getGameState().addCardToZone(_game, frodosPipe, Zone.HAND);
        _game.getGameState().addCardToZone(_game, pipeweed1, Zone.SUPPORT);
        _game.getGameState().addCardToZone(_game, pipeweed2, Zone.SUPPORT);

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

        PhysicalCardImpl athelas = new PhysicalCardImpl(_game, 100, "1_94", P1, _cardLibrary.getCardBlueprint("1_94"));
        PhysicalCardImpl aragorn = new PhysicalCardImpl(_game, 101, "1_89", P1, _cardLibrary.getCardBlueprint("1_89"));

        _game.getGameState().addCardToZone(_game, aragorn, Zone.FREE_CHARACTERS);
        _game.getGameState().attachCard(_game, athelas, aragorn);

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

        PhysicalCardImpl betterThanNothing = createCard(P2, "31_19");
        PhysicalCardImpl wizardIsNeverLate = createCard(P1, "30_23");

        _game.getGameState().addCardToZone(_game, wizardIsNeverLate, Zone.HAND);
        _game.getGameState().addCardToZone(_game, betterThanNothing, Zone.SUPPORT);

        skipMulligans();
        int burdens = 0;

        playerDecided(P1, "0");

        assertEquals(burdens, 0);
    }

}
