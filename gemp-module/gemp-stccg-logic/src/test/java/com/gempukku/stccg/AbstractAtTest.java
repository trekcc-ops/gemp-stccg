package com.gempukku.stccg;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardGeneric;
import com.gempukku.stccg.common.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.TribblesGame;
import org.junit.jupiter.api.Assertions;

import java.util.*;

@SuppressWarnings("MethodWithMultipleReturnPoints")
public abstract class AbstractAtTest extends AbstractLogicTest {

    protected ST1EGame _game;
    private TribblesGame _tribblesGame;
    protected UserFeedback _userFeedback;
    static final String P1 = "player1";
    static final String P2 = "player2";
    private FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);

    protected void initializeSimple1EGame(int deckSize) {
        Map<String, CardDeck> decks = new HashMap<>();
        CardDeck testDeck = new CardDeck("Test");
        for (int i = 0; i < deckSize; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, "101_104"); // Federation Outpost
        }

        decks.put(P1, testDeck);
        decks.put(P2, testDeck);

        GameFormat format = formatLibrary.getFormat("st1emoderncomplete");

        _game = new ST1EGame(format, decks, _cardLibrary);
        _userFeedback = _game.getUserFeedback();
        _game.startGame();

    }

    protected void initializeSimpleTribblesGame(int deckSize) {
        Map<String, CardDeck> decks = new HashMap<>();
        CardDeck testDeck = new CardDeck("Test");
        for (int i = 0; i < deckSize; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, "221_002"); // 1 Tribble - Kindness
        }

        decks.put(P1, testDeck);
        decks.put(P2, testDeck);

        GameFormat format = formatLibrary.getFormat("tribbles");

        _tribblesGame = new TribblesGame(format, decks, _cardLibrary);
        _userFeedback = _tribblesGame.getUserFeedback();
        _tribblesGame.startGame();

    }

    private Map<String, CardDeck> getIntroTwoPlayerDecks() {
        Map<String, CardDeck> decks = new HashMap<>();

        CardDeck fedDeck = new CardDeck("Federation");
        fedDeck.addCard(SubDeck.MISSIONS, "106_004"); // Cargo Rendezvous
        fedDeck.addCard(SubDeck.MISSIONS, "106_005"); // Distress Mission
        fedDeck.addCard(SubDeck.MISSIONS, "106_007"); // Gravesworld
        fedDeck.addCard(SubDeck.MISSIONS, "106_008"); // Homeward
        fedDeck.addCard(SubDeck.MISSIONS, "106_009"); // Hostage Situation
        fedDeck.addCard(SubDeck.MISSIONS, "106_013"); // Survey Instability
        fedDeck.addCard(SubDeck.SEED_DECK, "101_104"); // Federation Outpost
        // Dilemmas, Events & Interrupts
        // Engineering Kit
        // Medical Kit
        // Medical Tricorder
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_065"); // Tricorder
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_201"); // Calloway
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_202"); // Christopher Hobson
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_202"); // Christopher Hobson
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_203"); // Darian Wallace
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_203"); // Darian Wallace
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_213"); // Giusti
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_220"); // Linda Larson
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_222"); // McKnight
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_223"); // Mendon
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_223"); // Mendon
        fedDeck.addCard(SubDeck.DRAW_DECK, "103_096"); // Montgomery Scott
        // Simon Tarses
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_236"); // Sito Jaxa
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_242"); // Taitt
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_242"); // Taitt
        // Taurik
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_293"); // Dr. Farek
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_297"); // Gorta
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_300"); // Narik
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_303"); // Vekor
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_331"); // Runabout
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_332"); // Type VI Shuttlecraft
        // U.S.S. Galaxy x2
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_339"); // U.S.S. Nebula
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_339"); // U.S.S. Nebula


        CardDeck klingonDeck = new CardDeck("Klingon");
        klingonDeck.addCard(SubDeck.MISSIONS, "106_002"); // A Good Place to Die
        klingonDeck.addCard(SubDeck.MISSIONS, "106_003"); // Avert Danger
        klingonDeck.addCard(SubDeck.MISSIONS, "106_006"); // Gault
        klingonDeck.addCard(SubDeck.MISSIONS, "106_010"); // Reopen Dig
        klingonDeck.addCard(SubDeck.MISSIONS, "106_011"); // Reported Activity
        klingonDeck.addCard(SubDeck.MISSIONS, "106_012"); // Sensitive Search
        klingonDeck.addCard(SubDeck.SEED_DECK, "101_105"); // Klingon Outpost
        // Dilemmas, Events & Interrupts
        // Engineering Kit
        // Medical Kit
        // Medical Tricorder
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_065"); // Tricorder
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_253"); // B'iJik
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_254"); // Batrell
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_256"); // Divok
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_256"); // Divok
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_257"); // Dukath
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_260"); // Gorath
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_262"); // J'Ddan
        // K'Tesh x2
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_270"); // Klag
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_271"); // Kle'eg
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_271"); // Kle'eg
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_276"); // Kromm
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_286"); // Torin
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_286"); // Torin
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_288"); // Vekma
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_293"); // Dr. Farek
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_297"); // Gorta
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_300"); // Narik
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_303"); // Vekor
        klingonDeck.addCard(SubDeck.DRAW_DECK, "103_118"); // I.K.C. K'Ratak
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_347"); // I.K.S. K'Vort
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_347"); // I.K.S. K'Vort
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_347"); // I.K.S. K'Vort
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_350"); // I.K.C. Vor'Cha
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_350"); // I.K.C. Vor'Cha

        decks.put(P1, fedDeck);
        decks.put(P2, klingonDeck);

        return decks;
    }

    protected void initializeIntroductoryTwoPlayerGame() {
        Map<String, CardDeck> decks = getIntroTwoPlayerDecks();

        FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);
        GameFormat format = formatLibrary.getFormat("st1emoderncomplete");

        _game = new ST1EGame(format, decks, _cardLibrary);
        _userFeedback = _game.getUserFeedback();
        _game.startGame();
    }

    protected void initializeGameWithAttentionAllHands() {
        Map<String, CardDeck> decks = getIntroTwoPlayerDecks();
        decks.get(P1).addCard(SubDeck.DRAW_DECK, "155_021"); // Attention All Hands
        decks.get(P2).addCard(SubDeck.DRAW_DECK, "155_021");
        decks.get(P1).addCard(SubDeck.SEED_DECK, "178_044"); // Continuing Mission
        decks.get(P2).addCard(SubDeck.SEED_DECK, "178_044"); // Continuing Mission

        FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);
        GameFormat format = formatLibrary.getFormat("st1emoderncomplete");

        _game = new ST1EGame(format, decks, _cardLibrary);
        _userFeedback = _game.getUserFeedback();
        _game.startGame();
    }


    protected void validateContents(String[] array1, String[] array2) {
        if (array1.length != array2.length)
            Assertions.fail("Array sizes differ");
        Collection<String> values = new ArrayList<>(Arrays.asList(array1));
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

    protected void autoSeedMissions() throws DecisionResultInvalidException {
        // Both players keep picking option #1 until all missions are seeded
        while (_game.getGameState().getCurrentPhase() == Phase.SEED_MISSION) {
            if (_userFeedback.getAwaitingDecision(P1) != null) {
                playerDecided(P1, "0");
            } else if (_userFeedback.getAwaitingDecision(P2) != null) {
                playerDecided(P2, "0");
            }
        }
    }
    
    protected void autoSeedFacility() throws DecisionResultInvalidException {
        while (_game.getGameState().getCurrentPhase() == Phase.SEED_FACILITY) {
            if (_userFeedback.getAwaitingDecision(P1) != null) {
                if (_userFeedback.getAwaitingDecision(P1).getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
                    List<String> cardIdList = new java.util.ArrayList<>(Arrays.stream(_userFeedback.getAwaitingDecision(P1).getDecisionParameters().get("cardId")).toList());
                    Collections.shuffle(cardIdList);
                    playerDecided(P1, cardIdList.getFirst());
                }
                else
                    playerDecided(P1, "0");
            } else if (_userFeedback.getAwaitingDecision(P2) != null) {
                if (_userFeedback.getAwaitingDecision(P2).getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
                    List<String> cardIdList = new java.util.ArrayList<>(Arrays.stream(_userFeedback.getAwaitingDecision(P2).getDecisionParameters().get("cardId")).toList());
                    Collections.shuffle(cardIdList);
                    playerDecided(P2, cardIdList.getFirst());
                }
                else
                    playerDecided(P2, "0");
            }
        }
    }

}