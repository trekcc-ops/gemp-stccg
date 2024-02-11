package com.gempukku.stccg.at;

import com.gempukku.stccg.AbstractLogicTest;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.adventure.DefaultAdventureLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalCardImpl;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.DefaultUserFeedback;
import com.gempukku.stccg.game.TribblesGame;

import java.util.*;

import static org.junit.Assert.fail;

public abstract class AbstractAtTest extends AbstractLogicTest {

    protected DefaultGame _game;
    protected DefaultUserFeedback _userFeedback;
    public static final String P1 = "player1";
    public static final String P2 = "player2";

    protected PhysicalCardImpl createCard(String owner, String blueprintId) throws CardNotFoundException {
        return (PhysicalCardImpl) _game.getGameState().createPhysicalCard(owner, _cardLibrary, blueprintId);
    }

    protected void initializeSimplestGame() throws DecisionResultInvalidException {
        this.initializeSimplestGame(null);
    }

    protected void initializeSimplestGame(Map<String, Collection<String>> additionalCardsInDeck) throws DecisionResultInvalidException {
        Map<String, CardDeck> decks = new HashMap<>();
        addPlayerDeck(P1, decks, additionalCardsInDeck);
        addPlayerDeck(P2, decks, additionalCardsInDeck);

        initializeGameWithDecks(decks);
    }

    protected void initializeGameWithDecks(Map<String, CardDeck> decks) throws DecisionResultInvalidException {
        initializeGameWithDecks(decks, "multipath");
    }

    protected void initializeGameWithDecks(Map<String, CardDeck> decks, String formatName) throws DecisionResultInvalidException {
        _userFeedback = new DefaultUserFeedback();

        FormatLibrary formatLibrary = new FormatLibrary(new DefaultAdventureLibrary(), _cardLibrary);
        GameFormat format = formatLibrary.getFormat(formatName);

        Map<String, CardDeck> genericDecks = new HashMap<>();
        genericDecks.putAll(decks);

        _game = new TribblesGame(format, genericDecks, _userFeedback, _cardLibrary);
        _userFeedback.setGame(_game);
        _game.startGame();

        // Bidding
        playerDecided(P1, "1");
        playerDecided(P2, "0");

        // Seating choice
        playerDecided(P1, "0");
    }

    protected void skipMulligans() throws DecisionResultInvalidException {
        // Mulligans
        playerDecided(P1, "0");
        playerDecided(P2, "0");
    }

    protected void validateContents(String[] array1, String[] array2) {
        if (array1.length != array2.length)
            fail("Array sizes differ");
        List<String> values = new ArrayList<>(Arrays.asList(array1));
        for (String s : array2) {
            if (!values.remove(s))
                fail("Arrays contents differ");
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
        CardDeck deck = createSimplestDeck();
        if (additionalCardsInDeck != null) {
            Collection<String> extraCards = additionalCardsInDeck.get(player);
            if (extraCards != null)
                for (String extraCard : extraCards)
                    deck.addCard(extraCard);
        }
        decks.put(player, deck);
    }

    protected void moveCardToZone(PhysicalCardImpl card, Zone zone) {
        _game.getGameState().addCardToZone(_game, card, zone);
    }

    protected void playerDecided(String player, String answer) throws DecisionResultInvalidException {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(player);
        _userFeedback.participantDecided(player);
        try {
            decision.decisionMade(answer);
        } catch (DecisionResultInvalidException exp) {
            _userFeedback.sendAwaitingDecision(player, decision);
            throw exp;
        }
        _game.carryOutPendingActionsUntilDecisionNeeded();
    }

    protected void carryOutEffectInPhaseActionByPlayer(String playerId, Effect effect) throws DecisionResultInvalidException {
        SystemQueueAction action = new SystemQueueAction();
        action.appendEffect(effect);
        carryOutEffectInPhaseActionByPlayer(playerId, action);
    }

    protected void carryOutEffectInPhaseActionByPlayer(String playerId, Action action) throws DecisionResultInvalidException {
        CardActionSelectionDecision awaitingDecision = (CardActionSelectionDecision) _userFeedback.getAwaitingDecision(playerId);
        awaitingDecision.addAction(action);

        playerDecided(playerId, "0");
    }

    protected CardDeck createSimplestDeck() {
        CardDeck CardDeck = new CardDeck("Some deck");
        return CardDeck;
    }
}
