package com.gempukku.stccg;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.common.AwaitingDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.UserFeedback;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.common.GameFormat;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.DefaultUserFeedback;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public abstract class AbstractAtTest extends AbstractLogicTest {

    protected ST1EGame _game;
    protected UserFeedback _userFeedback;
    public static final String P1 = "player1";
    public static final String P2 = "player2";

    protected PhysicalCardGeneric createCard(String owner, String blueprintId) throws CardNotFoundException {
        return (PhysicalCardGeneric) _game.getGameState().createPhysicalCard(owner, _cardLibrary, blueprintId);
    }

    protected void initializeSimple1EGame(int deckSize) {
        Map<String, CardDeck> decks = new HashMap<>();
        CardDeck testDeck = new CardDeck("Test");
        for (int i = 0; i < deckSize; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, "101_104"); // Federation Outpost
        }

        decks.put(P1, testDeck);
        decks.put(P2, testDeck);

        FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);
        GameFormat format = formatLibrary.getFormat("st1emoderncomplete");

        _game = new ST1EGame(format, decks, _cardLibrary);
        _userFeedback = _game.getUserFeedback();
        _game.startGame();

    }

    protected void validateContents(String[] array1, String[] array2) {
        if (array1.length != array2.length)
            Assertions.fail("Array sizes differ");
        List<String> values = new ArrayList<>(Arrays.asList(array1));
        for (String s : array2) {
            if (!values.remove(s))
                Assertions.fail("Arrays contents differ");
        }
    }

    protected String[] toCardIdArray(PhysicalCard... cards) {
        String[] result = new String[cards.length];
        for (int i = 0; i < cards.length; i++)
            result[i] = String.valueOf(cards[i].getCardId());
        return result;
    }

    protected String getArbitraryCardId(AwaitingDecision awaitingDecision, String blueprintId) {
        String[] blueprints = awaitingDecision.getDecisionParameters().get("blueprintId");
        for (int i = 0; i < blueprints.length; i++)
            if (blueprints[i].equals(blueprintId))
                return ((String[]) awaitingDecision.getDecisionParameters().get("cardId"))[i];
        return null;
    }

    protected String getCardActionId(AwaitingDecision awaitingDecision, String actionTextStart) {
        String[] actionTexts = awaitingDecision.getDecisionParameters().get("actionText");
        for (int i = 0; i < actionTexts.length; i++)
            if (actionTexts[i].startsWith(actionTextStart))
                return ((String[]) awaitingDecision.getDecisionParameters().get("actionId"))[i];
        return null;
    }

    protected String getCardActionId(String playerId, String actionTextStart) {
        return getCardActionId(_userFeedback.getAwaitingDecision(playerId), actionTextStart);
    }

    protected String getCardActionIdContains(AwaitingDecision awaitingDecision, String actionTextContains) {
        String[] actionTexts = awaitingDecision.getDecisionParameters().get("actionText");
        for (int i = 0; i < actionTexts.length; i++)
            if (actionTexts[i].contains(actionTextContains))
                return ((String[]) awaitingDecision.getDecisionParameters().get("actionId"))[i];
        return null;
    }

    protected String getMultipleDecisionIndex(AwaitingDecision awaitingDecision, String result) {
        String[] actionTexts = awaitingDecision.getDecisionParameters().get("results");
        for (int i = 0; i < actionTexts.length; i++)
            if (actionTexts[i].equals(result))
                return String.valueOf(i);
        return null;
    }

    protected void addPlayerDeck(String player, Map<String, CardDeck> decks, Map<String, Collection<String>> additionalCardsInDeck) {
        CardDeck deck = new CardDeck("Some deck");
        if (additionalCardsInDeck != null) {
            Collection<String> extraCards = additionalCardsInDeck.get(player);
            if (extraCards != null)
                for (String extraCard : extraCards)
                    deck.addCard(extraCard);
        }
        decks.put(player, deck);
    }

    protected void moveCardToZone(PhysicalCardGeneric card, Zone zone) {
        _game.getGameState().addCardToZone(card, zone);
    }

    protected void playerDecided(String player, String answer) throws DecisionResultInvalidException {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(player);
        _game.getGameState().playerDecisionFinished(player, _userFeedback);
        try {
            decision.decisionMade(answer);
        } catch (DecisionResultInvalidException exp) {
            _userFeedback.sendAwaitingDecision(player, decision);
            throw exp;
        }
        _game.carryOutPendingActionsUntilDecisionNeeded();
    }

    protected void carryOutEffectInPhaseActionByPlayer(String playerId, Effect effect) throws DecisionResultInvalidException {
        SystemQueueAction action = new SystemQueueAction(_game);
        action.appendEffect(effect);
        carryOutEffectInPhaseActionByPlayer(playerId, action);
    }

    protected void carryOutEffectInPhaseActionByPlayer(String playerId, Action action) throws DecisionResultInvalidException {
        CardActionSelectionDecision awaitingDecision = (CardActionSelectionDecision) _userFeedback.getAwaitingDecision(playerId);
        awaitingDecision.addAction(action);

        playerDecided(playerId, "0");
    }

}
