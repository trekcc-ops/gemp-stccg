package com.gempukku.stccg;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.movecard.BeamCardsAction;
import com.gempukku.stccg.actions.movecard.UndockAction;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.actions.playcard.SeedCardAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.decisions.*;
import com.gempukku.stccg.formats.FormatLibrary;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Assertions;

import java.util.*;

@SuppressWarnings("MethodWithMultipleReturnPoints")
public abstract class AbstractAtTest extends AbstractLogicTest {

    protected ST1EGame _game;
    private TribblesGame _tribblesGame;
    protected UserFeedback _userFeedback;
    protected static final String P1 = "player1";
    protected static final String P2 = "player2";
    private FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);
    protected FacilityCard _outpost;
    protected MissionCard _mission;
    protected MissionCard _rogueComet;

    protected void initializeSimple1EGame(int deckSize) {
        Map<String, CardDeck> decks = new HashMap<>();
        CardDeck testDeck = new CardDeck("Test");
        for (int i = 0; i < deckSize; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, "101_104"); // Federation Outpost
        }

        decks.put(P1, testDeck);
        decks.put(P2, testDeck);

        GameFormat format = formatLibrary.getFormat("debug1e");

        _game = new ST1EGame(format, decks, _cardLibrary);
        _userFeedback = _game.getUserFeedback();
        _game.startGame();

    }

    protected void initializeSimple1EGame(int deckSize, String blueprintId) {
        Map<String, CardDeck> decks = new HashMap<>();
        CardDeck testDeck = new CardDeck("Test");
        for (int i = 0; i < deckSize; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, blueprintId); // Federation Outpost
        }

        decks.put(P1, testDeck);
        decks.put(P2, testDeck);

        GameFormat format = formatLibrary.getFormat("debug1e");

        _game = new ST1EGame(format, decks, _cardLibrary);
        _userFeedback = _game.getUserFeedback();
        _game.startGame();

    }


    protected void initializeSimple1EGameWithDoorways(int deckSize) {
        Map<String, CardDeck> decks = new HashMap<>();
        CardDeck testDeck = new CardDeck("Test");
        for (int i = 0; i < deckSize; i++) {
            testDeck.addCard(SubDeck.DRAW_DECK, "101_104"); // Federation Outpost
            testDeck.addCard(SubDeck.MISSIONS, "101_154"); // Excavation
        }
        testDeck.addCard(SubDeck.SEED_DECK, "105_015"); // Q-Flash

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
        // Alien Labyrinth
//        fedDeck.addCard(SubDeck.SEED_DECK, "101_012"); // Anaphasic Organism
        // Female's Love Interest
        // Hidden Entrance
        // Malfunctioning Door
        // Microvirus
        // Atmospheric Ionization
        // Pattern Enhancers
        // Plasma Fire
        // Res-Q
        // Spacedock
        // Tetryon Field
        // Yellow Alert
        // Countermanda
        // Disruptor Overload
        // Escape Pod
        // Kevin Uxbridge: Convergence
        // Long-Range Scan
        // Loss of Orbital Stability
        // Palor Toff: Alien Trader
        // Particle Fountain
        // Wormhole x2
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_055"); // Engineering Kit
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_060"); // Medical Kit
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_061"); // Medical Tricorder
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
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_236"); // Simon Tarses
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_239"); // Sito Jaxa
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_242"); // Taitt
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_242"); // Taitt
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_245"); // Taurik
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_293"); // Dr. Farek
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_297"); // Gorta
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_300"); // Narik
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_303"); // Vekor
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_331"); // Runabout
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_332"); // Type VI Shuttlecraft
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_336"); // U.S.S. Galaxy
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_336"); // U.S.S. Galaxy
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
        klingonDeck.addCard(SubDeck.SEED_DECK, "101_014"); // Archer
        klingonDeck.addCard(SubDeck.SEED_DECK, "103_014"); // Ferengi Attack
        // Hunter Gangs
        // Impassable Door
        // Male's Love Interest
        // The Gatherers
        // Atmospheric Ionization x2
        // Pattern Enhancers
        // Plasma Fire
        // Res-Q
        // Where No One Has Gone Before
        // Yellow Alert
        // Countermanda
        // Disruptor Overload
        // Kevin Uxbridge: Convergence
        // Long-Range Scan
        // Loss of Orbital Stability
        // Palor Toff: Alien Trader
        // Particle Fountain
        // Ship Seizure
        // Wormhole x2
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_055"); // Engineering Kit
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_060"); // Medical Kit
        klingonDeck.addCard(SubDeck.DRAW_DECK, "101_061"); // Medical Tricorder
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
        GameFormat format = formatLibrary.getFormat("debug1e");

        _game = new ST1EGame(format, decks, _cardLibrary);
        _userFeedback = _game.getUserFeedback();
        _game.startGame();
    }

    protected void initializeQuickMissionAttempt(String missionTitle) throws DecisionResultInvalidException {
        initializeGameToTestMissionAttempt();

        autoSeedMissions();
        while (_game.getCurrentPhase() == Phase.SEED_DILEMMA) {
            skipDilemma();
        }

        for (PhysicalCard card : _game.getGameState().getAllCardsInGame()) {
            if (Objects.equals(card.getTitle(), "Federation Outpost") && card instanceof FacilityCard facility)
                _outpost = facility;
            if (Objects.equals(card.getTitle(), missionTitle) && card instanceof MissionCard mission)
                _mission = mission;
            if (Objects.equals(card.getTitle(), "Investigate Rogue Comet") && card instanceof MissionCard mission) {
                _rogueComet = mission;
            }
        }
    }

    protected void initializeGameToTestAMS() {
        Map<String, CardDeck> decks = new HashMap<>();

        CardDeck fedDeck = new CardDeck("Federation");
        fedDeck.addCard(SubDeck.MISSIONS, "101_154"); // Excavation
        fedDeck.addCard(SubDeck.SEED_DECK, "101_104"); // Federation Outpost
        fedDeck.addCard(SubDeck.SEED_DECK, "109_063"); // AMS
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_215"); // Jean-Luc Picard
        for (int i = 0; i < 35; i++)
            fedDeck.addCard(SubDeck.DRAW_DECK, "101_236"); // Simon Tarses
        for (int i = 0; i < 35; i++)
            fedDeck.addCard(SubDeck.DRAW_DECK, "101_203"); // Darian Wallace
        decks.put(P1, fedDeck);

        CardDeck klingonDeck = new CardDeck("Klingon");
        klingonDeck.addCard(SubDeck.MISSIONS, "106_006"); // Gault
        for (int i = 0; i < 35; i++)
            klingonDeck.addCard(SubDeck.DRAW_DECK, "101_271"); // Kle'eg
        decks.put(P2, klingonDeck);


        FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);
        GameFormat format = formatLibrary.getFormat("debug1e");

        _game = new ST1EGame(format, decks, _cardLibrary);
        _userFeedback = _game.getUserFeedback();
        _game.startGame();
    }

    protected void initializeGameToTestMissionAttempt() {
        Map<String, CardDeck> decks = new HashMap<>();

        CardDeck fedDeck = new CardDeck("Federation");
        fedDeck.addCard(SubDeck.MISSIONS, "101_154"); // Excavation
        fedDeck.addCard(SubDeck.MISSIONS, "101_171"); // Investigate Rogue Comet
        fedDeck.addCard(SubDeck.SEED_DECK, "101_104"); // Federation Outpost
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_215"); // Jean-Luc Picard
        for (int i = 0; i < 35; i++)
            fedDeck.addCard(SubDeck.DRAW_DECK, "101_236"); // Simon Tarses
        for (int i = 0; i < 35; i++)
            fedDeck.addCard(SubDeck.DRAW_DECK, "101_203"); // Darian Wallace
        decks.put(P1, fedDeck);

        CardDeck klingonDeck = new CardDeck("Klingon");
        klingonDeck.addCard(SubDeck.MISSIONS, "106_006"); // Gault
        for (int i = 0; i < 35; i++)
            klingonDeck.addCard(SubDeck.DRAW_DECK, "101_271"); // Kle'eg
        decks.put(P2, klingonDeck);


        FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);
        GameFormat format = formatLibrary.getFormat("debug1e");

        _game = new ST1EGame(format, decks, _cardLibrary);
        _userFeedback = _game.getUserFeedback();
        _game.startGame();
    }

    protected PhysicalCard newCardForGame(String blueprintId, String playerId) throws CardNotFoundException {
        return _game.getGameState().addCardToGame(blueprintId, _cardLibrary, playerId);
    }

    protected void initializeQuickMissionAttemptWithRisk() {
        Map<String, CardDeck> decks = new HashMap<>();

        CardDeck fedDeck = new CardDeck("Federation");
        fedDeck.addCard(SubDeck.MISSIONS, "101_154"); // Excavation
        fedDeck.addCard(SubDeck.SEED_DECK, "101_104"); // Federation Outpost
        fedDeck.addCard(SubDeck.SEED_DECK, "212_019"); // Risk is Our Business
        fedDeck.addCard(SubDeck.DRAW_DECK, "101_215"); // Jean-Luc Picard
        for (int i = 0; i < 35; i++)
            fedDeck.addCard(SubDeck.DRAW_DECK, "101_236"); // Simon Tarses
        decks.put(P1, fedDeck);

        CardDeck klingonDeck = new CardDeck("Klingon");
        klingonDeck.addCard(SubDeck.MISSIONS, "106_006"); // Gault
        for (int i = 0; i < 35; i++)
            klingonDeck.addCard(SubDeck.DRAW_DECK, "101_271"); // Kle'eg
        decks.put(P2, klingonDeck);


        FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);
        GameFormat format = formatLibrary.getFormat("debug1e");

        _game = new ST1EGame(format, decks, _cardLibrary);
        _userFeedback = _game.getUserFeedback();
        _game.startGame();
    }



    protected void initializeGameWithAttentionAllHands() {
        Map<String, CardDeck> decks = getIntroTwoPlayerDecks();
        decks.get(P1).addCard(SubDeck.DRAW_DECK, "155_021"); // Attention All Hands
        decks.get(P2).addCard(SubDeck.DRAW_DECK, "155_021");
        decks.get(P1).addCard(SubDeck.SEED_DECK, "155_022"); // Continuing Mission
        decks.get(P2).addCard(SubDeck.SEED_DECK, "155_022");

        FormatLibrary formatLibrary = new FormatLibrary(_cardLibrary);
        GameFormat format = formatLibrary.getFormat("debug1e");

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

    protected void playerDecided(String player, String answer) throws DecisionResultInvalidException {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(player);
        _game.getGameState().playerDecisionFinished(player, _userFeedback);
        try {
            decision.decisionMade(answer);
        } catch (DecisionResultInvalidException exp) {
            _userFeedback.sendAwaitingDecision(decision);
            throw exp;
        }
        _game.carryOutPendingActionsUntilDecisionNeeded();
    }

    protected void skipCardPlay() throws DecisionResultInvalidException {
        String playerId = _game.getCurrentPlayerId();
        while (_game.getCurrentPhase() == Phase.CARD_PLAY) {
            if (_userFeedback.getAwaitingDecision(playerId) != null)
                playerDecided(playerId, "");
        }
    }

    protected void skipExecuteOrders() throws DecisionResultInvalidException {
        String playerId = _game.getCurrentPlayerId();
        while (_game.getCurrentPhase() == Phase.EXECUTE_ORDERS) {
            if (_userFeedback.getAwaitingDecision(playerId) != null)
                playerDecided(playerId, "");
        }
    }


    protected void skipDilemma() throws DecisionResultInvalidException {
        for (String playerId : _game.getAllPlayerIds())
            if (_userFeedback.getAwaitingDecision(playerId) != null)
                playerDecided(playerId, "");
    }

    protected void seedDilemma(PhysicalCard seedCard, PhysicalCard mission) throws DecisionResultInvalidException {
        Player player = seedCard.getOwner();
        int cardId = mission.getCardId();
        AwaitingDecision missionSelection = _userFeedback.getAwaitingDecision(player.getPlayerId());
        Map<String, String[]> decisionParameters = missionSelection.getDecisionParameters();
        String decisionId = null;
        for (int i = 0; i < decisionParameters.get("actionId").length; i++) {
            if (Objects.equals(decisionParameters.get("cardId")[i], String.valueOf(cardId)) &&
                    decisionParameters.get("actionText")[i].startsWith("Seed cards under")) {
                decisionId = String.valueOf(i);
            }
        }
        playerDecided(player.getPlayerId(), decisionId);

        playerDecided(player.getPlayerId(), String.valueOf(seedCard.getCardId()));
    }

    protected void removeDilemma(PhysicalCard seedCard, PhysicalCard mission) throws DecisionResultInvalidException,
            InvalidGameLogicException {
        Player player = seedCard.getOwner();
        int cardId = mission.getCardId();
        AwaitingDecision missionSelection = _userFeedback.getAwaitingDecision(player.getPlayerId());
        Map<String, String[]> decisionParameters = missionSelection.getDecisionParameters();
        String decisionId = null;
        for (int i = 0; i < decisionParameters.get("actionId").length; i++) {
            if (Objects.equals(decisionParameters.get("cardId")[i], String.valueOf(cardId)) &&
                    decisionParameters.get("actionText")[i].startsWith("Remove seed cards from")) {
                decisionId = String.valueOf(i);
            }
        }
        playerDecided(player.getPlayerId(), decisionId);

        if (_userFeedback.getAwaitingDecision(player.getPlayerId()) instanceof ArbitraryCardsSelectionDecision dilemmaSelection)
            playerDecided(player.getPlayerId(), dilemmaSelection.getCardIdForCard(seedCard));
        else throw new InvalidGameLogicException("Player decision is not the expected type");
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

    protected void autoSeedDoorway() throws DecisionResultInvalidException {
        while (_game.getGameState().getCurrentPhase() == Phase.SEED_DOORWAY) {
            if (_userFeedback.getAwaitingDecision(P1) != null) {
                if (_userFeedback.getAwaitingDecision(P1).getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
                    List<String> cardIdList = new java.util.ArrayList<>(Arrays.stream(_userFeedback.getAwaitingDecision(P1).getDecisionParameters().get("cardId")).toList());
                    playerDecided(P1, cardIdList.getFirst());
                }
                else
                    playerDecided(P1, "0");
            } else if (_userFeedback.getAwaitingDecision(P2) != null) {
                if (_userFeedback.getAwaitingDecision(P2).getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
                    List<String> cardIdList = new java.util.ArrayList<>(Arrays.stream(_userFeedback.getAwaitingDecision(P2).getDecisionParameters().get("cardId")).toList());
                    playerDecided(P2, cardIdList.getFirst());
                }
                else
                    playerDecided(P2, "0");
            }
        }
    }

    protected void autoSeedFacility() throws DecisionResultInvalidException {
        while (_game.getGameState().getCurrentPhase() == Phase.SEED_FACILITY) {
            if (_userFeedback.getAwaitingDecision(P1) != null) {
                if (_userFeedback.getAwaitingDecision(P1).getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
                    List<String> cardIdList = new java.util.ArrayList<>(Arrays.stream(_userFeedback.getAwaitingDecision(P1).getDecisionParameters().get("cardId")).toList());
                    try {
                        playerDecided(P1, cardIdList.getFirst());
                    } catch(Exception exp) {
                        int x = 5;
                        int y = x + 2;
                    }
                }
                else
                    playerDecided(P1, "0");
            } else if (_userFeedback.getAwaitingDecision(P2) != null) {
                if (_userFeedback.getAwaitingDecision(P2).getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
                    List<String> cardIdList = new java.util.ArrayList<>(Arrays.stream(_userFeedback.getAwaitingDecision(P2).getDecisionParameters().get("cardId")).toList());
                    playerDecided(P2, cardIdList.getFirst());
                }
                else
                    playerDecided(P2, "0");
            }
        }
    }

    protected void seedFacility(String playerId, PhysicalCard cardToSeed, MissionLocation destination)
            throws DecisionResultInvalidException {
        SeedOutpostAction choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof SeedOutpostAction seedAction && seedAction.getCardToSeed() == cardToSeed) {
                    choice = seedAction;
                }
            }
            choice.setDestination(destination);
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to seed " + cardToSeed.getTitle());
    }

    protected void reportCard(String playerId, PhysicalCard cardToReport, FacilityCard destination)
            throws DecisionResultInvalidException {
        ReportCardAction choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof ReportCardAction reportAction &&
                        reportAction.getCardReporting() == cardToReport) {
                    choice = reportAction;
                }
            }
            choice.setDestination(destination);
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to report " + cardToReport.getTitle());
    }

    protected void playCard(String playerId, PhysicalCard cardToPlay)
            throws DecisionResultInvalidException {
        PlayCardAction choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof PlayCardAction playCardAction &&
                        playCardAction.getCardEnteringPlay() == cardToPlay) {
                    choice = playCardAction;
                }
            }
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to play " + cardToPlay.getTitle());
    }

    protected void beamCard(String playerId, PhysicalCard cardWithTransporters, PhysicalReportableCard1E cardToBeam,
                            PhysicalCard destination)
            throws DecisionResultInvalidException {
        BeamCardsAction choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof BeamCardsAction beamAction &&
                        beamAction.getCardUsingTransporters() == cardWithTransporters)
                    choice = beamAction;
            }
            choice.setOrigin(cardWithTransporters);
            choice.setCardsToMove(Collections.singletonList(cardToBeam));
            choice.setDestination(destination);
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to beam " + cardToBeam.getTitle());
    }

    protected void undockShip(String playerId, PhysicalShipCard ship)
            throws DecisionResultInvalidException {
        UndockAction choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof UndockAction undockAction &&
                        undockAction.getCardToMove() == ship)
                    choice = undockAction;
            }
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to undock " + ship.getTitle());
    }


    protected void beamCards(String playerId, PhysicalCard cardWithTransporters,
                             Collection<? extends PhysicalReportableCard1E> cardsToBeam, PhysicalCard destination)
            throws DecisionResultInvalidException {
        BeamCardsAction choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof BeamCardsAction beamAction &&
                        beamAction.getCardUsingTransporters() == cardWithTransporters)
                    choice = beamAction;
            }
            choice.setOrigin(cardWithTransporters);
            choice.setCardsToMove(cardsToBeam);
            choice.setDestination(destination);
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null) {
            throw new DecisionResultInvalidException(
                    "No valid action to beam " + TextUtils.getConcatenatedCardLinks(cardsToBeam));
        }
    }


    protected void seedCard(String playerId, PhysicalCard cardToSeed) throws DecisionResultInvalidException {
        Action choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof SeedCardAction seedAction &&
                        seedAction.getCardEnteringPlay() == cardToSeed)
                    choice = seedAction;
                if (action instanceof SeedOutpostAction seedAction &&
                        seedAction.getCardEnteringPlay() == cardToSeed)
                    choice = seedAction;
            }
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to seed " + cardToSeed.getTitle());
    }

    protected void chooseOnlyAction(String playerId) throws DecisionResultInvalidException {
        Action choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            if (actionDecision.getActions().size() == 1) {
                choice = actionDecision.getActions().getFirst();
                actionDecision.decisionMade(choice);
                _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
                _game.carryOutPendingActionsUntilDecisionNeeded();
            }
        }
        if (choice == null)
            throw new DecisionResultInvalidException("Could not choose a valid action");
    }


    protected void attemptMission(String playerId, AttemptingUnit attemptingUnit, MissionCard mission)
            throws DecisionResultInvalidException, InvalidGameLogicException {
        AttemptMissionAction choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof AttemptMissionAction attemptAction &&
                        attemptAction.getMission() == mission.getLocation())
                    choice = attemptAction;
            }
            choice.setAttemptingUnit(attemptingUnit);
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to attempt " + mission.getTitle());
    }

    protected void attemptMission(String playerId, MissionLocation mission)
            throws DecisionResultInvalidException {
        AttemptMissionAction choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action instanceof AttemptMissionAction attemptAction &&
                        attemptAction.getMission() == mission)
                    choice = attemptAction;
            }
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action to attempt " + mission.getLocationName());
    }

    protected <T extends Action> T selectAction(Class<T> clazz, PhysicalCard card, String playerId)
            throws DecisionResultInvalidException {
        T choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof ActionDecision actionDecision) {
            for (TopLevelSelectableAction action : actionDecision.getActions()) {
                if (action.getClass() == clazz && action.getCardForActionSelection() == card)
                    choice = (T) action;
            }
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null)
            throw new DecisionResultInvalidException("No valid action found");
        else return choice;
    }

    protected void selectCard(String playerId, PhysicalCard card) throws DecisionResultInvalidException {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof CardsSelectionDecision cardSelection) {
            cardSelection.decisionMade(card);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        } else if (decision instanceof ArbitraryCardsSelectionDecision arbitrary) {
            arbitrary.decisionMade(card);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
    }

    protected void selectCards(String playerId, List<PhysicalCard> cards) throws DecisionResultInvalidException {
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof CardsSelectionDecision cardSelection) {
            cardSelection.decisionMade(cards);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        } else if (decision instanceof ArbitraryCardsSelectionDecision arbitrary) {
            arbitrary.decisionMade(cards);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
    }

    protected void useGameText(PhysicalCard card, String playerId) throws DecisionResultInvalidException {
        Action choice = null;
        AwaitingDecision decision = _userFeedback.getAwaitingDecision(playerId);
        if (decision instanceof CardActionSelectionDecision actionDecision) {
            for (Action action : actionDecision.getActions()) {
                if (action.getPerformingCard() == card)
                    choice = action;
            }
            actionDecision.decisionMade(choice);
            _game.getGameState().playerDecisionFinished(playerId, _userFeedback);
            _game.carryOutPendingActionsUntilDecisionNeeded();
        }
        if (choice == null) {
            throw new DecisionResultInvalidException("Could not find game text action");
        }
    }


}