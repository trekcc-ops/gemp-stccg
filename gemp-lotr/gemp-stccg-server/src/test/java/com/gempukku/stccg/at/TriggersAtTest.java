package com.gempukku.stccg.at;

import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.PhysicalCardImpl;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.AwaitingDecisionType;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.modifiers.KeywordModifier;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TriggersAtTest extends AbstractAtTest {
    @Test
    public void fpCharWinsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl gimli = new PhysicalCardImpl(_game, 100, "5_7", P1, _cardLibrary.getCardBlueprint("5_7"));
        PhysicalCardImpl stoutAndStrong = new PhysicalCardImpl(_game, 101, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl goblinRunner = new PhysicalCardImpl(_game, 102, "1_178", P2, _cardLibrary.getCardBlueprint("1_178"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, gimli, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, stoutAndStrong, Zone.SUPPORT);

        // End fellowship phase
        assertEquals(Phase.FELLOWSHIP, _game.getGameState().getCurrentPhase());
        playerDecided(P1, "");

        _game.getGameState().addCardToZone(_game, goblinRunner, Zone.SHADOW_CHARACTERS);

        // End shadow phase
        assertEquals(Phase.SHADOW, _game.getGameState().getCurrentPhase());
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
        playerDecided(P1, gimli.getCardId() + " " + goblinRunner.getCardId());

        // Start skirmish
        playerDecided(P1, String.valueOf(gimli.getCardId()));

        // End skirmish phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        AwaitingDecision chooseTriggersDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, chooseTriggersDecision.getDecisionType());
        validateContents(new String[]{String.valueOf(gimli.getCardId()), String.valueOf(stoutAndStrong.getCardId())}, chooseTriggersDecision.getDecisionParameters().get("cardId"));
    }

    @Test
    public void musterWorkingWithOtherOptionalStartOfRegroupTrigger() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl dervorin = new PhysicalCardImpl(_game, 100, "7_88", P1, _cardLibrary.getCardBlueprint("7_88"));
        PhysicalCardImpl boromir = new PhysicalCardImpl(_game, 101, "1_96", P1, _cardLibrary.getCardBlueprint("1_96"));
        PhysicalCardImpl cardInHand1 = new PhysicalCardImpl(_game, 102, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand2 = new PhysicalCardImpl(_game, 103, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand3 = new PhysicalCardImpl(_game, 104, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand4 = new PhysicalCardImpl(_game, 105, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, dervorin, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, boromir, Zone.FREE_CHARACTERS);
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                new KeywordModifier(dervorin, dervorin, Keyword.MUSTER));

        _game.getGameState().addCardToZone(_game, cardInHand1, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand2, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand3, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand4, Zone.HAND);

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
        playerDecided(P2, "");

        final AwaitingDecision optionalStartOfRegroupDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalStartOfRegroupDecision.getDecisionType());
        validateContents(new String[]{String.valueOf(dervorin.getCardId()), String.valueOf(dervorin.getCardId())}, optionalStartOfRegroupDecision.getDecisionParameters().get("cardId"));

        playerDecided(P1, getCardActionId(optionalStartOfRegroupDecision, "Optional "));

        final AwaitingDecision optionalSecondStartOfRegroupDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalSecondStartOfRegroupDecision.getDecisionType());
        validateContents(new String[]{String.valueOf(dervorin.getCardId())}, optionalSecondStartOfRegroupDecision.getDecisionParameters().get("cardId"));
    }

    @Test
    public void userOfMusterAllowsUseOfOtherOptionalStartOfRegroupTrigger() throws DecisionResultInvalidException, CardNotFoundException {
        Map<String, Collection<String>> extraCards = new HashMap<>();
        initializeSimplestGame(extraCards);

        PhysicalCardImpl dervorin = new PhysicalCardImpl(_game, 100, "7_88", P1, _cardLibrary.getCardBlueprint("7_88"));
        PhysicalCardImpl boromir = new PhysicalCardImpl(_game, 101, "1_96", P1, _cardLibrary.getCardBlueprint("1_96"));
        PhysicalCardImpl cardInHand1 = new PhysicalCardImpl(_game, 102, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand2 = new PhysicalCardImpl(_game, 103, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand3 = new PhysicalCardImpl(_game, 104, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand4 = new PhysicalCardImpl(_game, 105, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand5 = new PhysicalCardImpl(_game, 106, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, dervorin, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, boromir, Zone.FREE_CHARACTERS);
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                new KeywordModifier(dervorin, dervorin, Keyword.MUSTER));

        _game.getGameState().addCardToZone(_game, cardInHand1, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand2, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand3, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand4, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand5, Zone.HAND);

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
        playerDecided(P2, "");

        final AwaitingDecision optionalStartOfRegroupDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalStartOfRegroupDecision.getDecisionType());
        validateContents(new String[]{String.valueOf(dervorin.getCardId())}, optionalStartOfRegroupDecision.getDecisionParameters().get("cardId"));

        playerDecided(P1, getCardActionId(optionalStartOfRegroupDecision, "Use "));

        playerDecided(P1, String.valueOf(cardInHand1.getCardId()));

        final AwaitingDecision optionalSecondStartOfRegroupDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalSecondStartOfRegroupDecision.getDecisionType());
        validateContents(new String[]{String.valueOf(dervorin.getCardId())}, optionalSecondStartOfRegroupDecision.getDecisionParameters().get("cardId"));
        assertTrue(((String[]) optionalSecondStartOfRegroupDecision.getDecisionParameters().get("actionText"))[0].startsWith("Optional "));
    }

    @Test
    public void userOfMusterDisablesUseOfOtherOptionalStartOfRegroupTrigger() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl dervorin = new PhysicalCardImpl(_game, 100, "7_88", P1, _cardLibrary.getCardBlueprint("7_88"));
        PhysicalCardImpl boromir = new PhysicalCardImpl(_game, 101, "1_96", P1, _cardLibrary.getCardBlueprint("1_96"));
        PhysicalCardImpl cardInHand1 = new PhysicalCardImpl(_game, 102, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand2 = new PhysicalCardImpl(_game, 103, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand3 = new PhysicalCardImpl(_game, 104, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));
        PhysicalCardImpl cardInHand4 = new PhysicalCardImpl(_game, 105, "4_57", P1, _cardLibrary.getCardBlueprint("4_57"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, dervorin, Zone.FREE_CHARACTERS);
        _game.getGameState().addCardToZone(_game, boromir, Zone.FREE_CHARACTERS);
        _game.getModifiersEnvironment().addUntilEndOfTurnModifier(
                new KeywordModifier(dervorin, dervorin, Keyword.MUSTER));

        _game.getGameState().addCardToZone(_game, cardInHand1, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand2, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand3, Zone.HAND);
        _game.getGameState().addCardToZone(_game, cardInHand4, Zone.HAND);

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
        playerDecided(P2, "");

        final AwaitingDecision optionalStartOfRegroupDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, optionalStartOfRegroupDecision.getDecisionType());
        validateContents(new String[]{String.valueOf(dervorin.getCardId()), String.valueOf(dervorin.getCardId())}, optionalStartOfRegroupDecision.getDecisionParameters().get("cardId"));

        playerDecided(P1, getCardActionId(optionalStartOfRegroupDecision, "Use "));
        playerDecided(P1, String.valueOf(cardInHand1.getCardId()));

        final AwaitingDecision regroupPhaseActionDecision = _userFeedback.getAwaitingDecision(P1);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, regroupPhaseActionDecision.getDecisionType());
        validateContents(new String[]{}, regroupPhaseActionDecision.getDecisionParameters().get("cardId"));

        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
    }

    @Test
    public void musterForShadowSideTriggersCorrectly() throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimplestGame();

        PhysicalCardImpl musterWitchKing = new PhysicalCardImpl(_game, 100, "11_226", P2, _cardLibrary.getCardBlueprint("11_226"));
        PhysicalCardImpl musterWitchKing2 = new PhysicalCardImpl(_game, 101, "11_226", P2, _cardLibrary.getCardBlueprint("11_226"));

        skipMulligans();

        _game.getGameState().addCardToZone(_game, musterWitchKing, Zone.SHADOW_CHARACTERS);
        _game.getGameState().addCardToZone(_game, musterWitchKing2, Zone.HAND);

        // End fellowship phase
        playerDecided(P1, "");

        // End shadow phase
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
        playerDecided(P1, "");
        playerDecided(P2, "");

        // End fierce assignment phase
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Fierce assign
        playerDecided(P1, "");
        playerDecided(P2, "");

        // Start regroup
        assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
    }

}
