package com.gempukku.lotro.game;

import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.actions.ActionsEnvironment;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.gamestate.GameStateListener;
import com.gempukku.lotro.gamestate.GameStats;
import com.gempukku.lotro.gamestate.TribblesGameState;
import com.gempukku.lotro.gamestate.UserFeedback;
import com.gempukku.lotro.modifiers.ModifiersEnvironment;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.modifiers.ModifiersQuerying;
import com.gempukku.lotro.processes.TribblesTurnProcedure;
import com.gempukku.lotro.rules.tribbles.TribblesRuleSet;

import java.util.*;

public class TribblesGame implements DefaultGame {
    private final TribblesGameState _gameState;
    private final ModifiersLogic _modifiersLogic = new ModifiersLogic();
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final UserFeedback _userFeedback;
    private final TribblesTurnProcedure _turnProcedure;
    private boolean _cancelled;
    private boolean _finished;

    private final LotroFormat _format;

    private final Set<String> _allPlayers;
    private final Map<String, Set<Phase>> _autoPassConfiguration = new HashMap<>();

    private String _winnerPlayerId;
    private final Map<String, String> _losers = new HashMap<>();

    private final Set<GameResultListener> _gameResultListeners = new HashSet<>();

    private final Set<String> _requestedCancel = new HashSet<>();
    private final CardBlueprintLibrary _library;

    public TribblesGame(LotroFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                        final CardBlueprintLibrary library) {
        _userFeedback = userFeedback;
        _library = library;
        _format = format;

        _allPlayers = decks.keySet();
        final Map<String, List<String>> cards = new HashMap<>();

        for (String playerId : _allPlayers) {

            CardDeck playerDeck = decks.get(playerId);
            List<String> deck = new LinkedList<>(playerDeck.getDrawDeckCards());

            cards.put(playerId, deck);
        }

        _gameState = new TribblesGameState();
        Stack<Action> _actionStack = new Stack<>();
        _actionsEnvironment = new DefaultActionsEnvironment(this, _actionStack);
        format.getAdventure().applyAdventureRules(this, _actionsEnvironment, _modifiersLogic);
        TribblesRuleSet ruleSet = new TribblesRuleSet(_actionsEnvironment, _modifiersLogic);
        ruleSet.applyRuleSet();

        _turnProcedure = new TribblesTurnProcedure(this, decks, userFeedback, _library, _actionStack,
                new PlayerOrderFeedback() {
                    @Override
                    public void setPlayerOrder(PlayerOrder playerOrder, String firstPlayer) {
                        final GameStats gameStats = _turnProcedure.getGameStats();
                        _gameState.init(playerOrder, firstPlayer, cards, library, format);
                    }
                });
    }


    @Override
    public boolean shouldAutoPass(String playerId, Phase phase) {
        return false;
    }

    @Override
    public boolean isSolo() {
        return _allPlayers.size() == 1;
    }

    public void addGameResultListener(GameResultListener listener) {
        _gameResultListeners.add(listener);
    }

    public void removeGameResultListener(GameResultListener listener) {
        _gameResultListeners.remove(listener);
    }

    @Override
    public LotroFormat getFormat() {
        return _format;
    }

    public void startGame() {
        if (!_cancelled)
            _turnProcedure.carryOutPendingActionsUntilDecisionNeeded();
    }

    public void carryOutPendingActionsUntilDecisionNeeded() {
        if (!_cancelled)
            _turnProcedure.carryOutPendingActionsUntilDecisionNeeded();
    }

    @Override
    public String getWinnerPlayerId() {
        return _winnerPlayerId;
    }

    public boolean isFinished() {
        return _finished;
    }

    public void cancelGame() {
        if (!_finished) {
            _cancelled = true;

            if (_gameState != null) {
                _gameState.sendMessage("Game was cancelled due to an error, the error was logged and will be fixed soon.");
                _gameState.sendMessage("Please post the replay game link and description of what happened on the TLHH forum.");
            }

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public void cancelGameRequested() {
        if (!_finished) {
            _cancelled = true;

            if (_gameState != null)
                _gameState.sendMessage("Game was cancelled, as requested by all parties.");

            for (GameResultListener gameResultListener : _gameResultListeners)
                gameResultListener.gameCancelled();

            _finished = true;
        }
    }

    public boolean isCancelled() {
        return _cancelled;
    }

    @Override
    public void playerWon(String playerId, String reason) {
        if (!_finished) {
            // Any remaining players have lost
            Set<String> losers = new HashSet<>(_allPlayers);
            losers.removeAll(_losers.keySet());
            losers.remove(playerId);

            for (String loser : losers)
                _losers.put(loser, "Other player won");

            gameWon(playerId, reason);
        }
    }

    private void gameWon(String winner, String reason) {
        _winnerPlayerId = winner;
        if (_gameState != null)
            _gameState.sendMessage(_winnerPlayerId + " is the winner due to: " + reason);

        assert _gameState != null;
        _gameState.finish();

        for (GameResultListener gameResultListener : _gameResultListeners)
            gameResultListener.gameFinished(_winnerPlayerId, reason, _losers);

        _finished = true;
    }

    @Override
    public void playerLost(String playerId, String reason) {
        if (!_finished) {
            if (_losers.get(playerId) == null) {
                _losers.put(playerId, reason);
                if (_gameState != null)
                    _gameState.sendMessage(playerId + " lost due to: " + reason);

                if (_losers.size() + 1 == _allPlayers.size()) {
                    List<String> allPlayers = new LinkedList<>(_allPlayers);
                    allPlayers.removeAll(_losers.keySet());
                    gameWon(allPlayers.get(0), "Last remaining player in game");
                }
            }
        }
    }

    public void requestCancel(String playerId) {
        _requestedCancel.add(playerId);
        if (_requestedCancel.size() == _allPlayers.size())
            cancelGameRequested();
    }

    @Override
    public TribblesGameState getGameState() {
        return _gameState;
    }

    @Override
    public CardBlueprintLibrary getLotroCardBlueprintLibrary() {
        return _library;
    }

    @Override
    public ActionsEnvironment getActionsEnvironment() {
        return _actionsEnvironment;
    }

    @Override
    public ModifiersEnvironment getModifiersEnvironment() {
        return _modifiersLogic;
    }

    @Override
    public ModifiersQuerying getModifiersQuerying() {
        return _modifiersLogic;
    }

    @Override
    public UserFeedback getUserFeedback() {
        return _userFeedback;
    }

    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        _gameState.addGameStateListener(playerId, gameStateListener, _turnProcedure.getGameStats());
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameState.removeGameStateListener(gameStateListener);
    }

    public void setPlayerAutoPassSettings(String playerId, Set<Phase> phases) {
        _autoPassConfiguration.put(playerId, phases);
    }

    public boolean checkPlayRequirements(LotroPhysicalCard card) {
//        _gameState.sendMessage("Calling game.checkPlayRequirements for card " + card.getBlueprint().getTitle());

        // Check if card's own play requirements are met
        if (card.getBlueprint().playRequirementsNotMet(this, card))
            return false;
        // Check if the card's playability has been modified in the current game state
        if (_modifiersLogic.canNotPlayCard(this, card.getOwner(), card))
            return false;

        // Otherwise, the play requirements are met if the card is next in the tribble sequence,
        // or if it can be played out of sequence
        return (isNextInSequence(card) || card.getBlueprint().canPlayOutOfSequence(this, card));
    }

    public boolean isNextInSequence(LotroPhysicalCard card) {
        final int cardValue = card.getBlueprint().getTribbleValue();
        if (_gameState.isChainBroken() && (cardValue == 1)) {
            return true;
        }
        return (cardValue == _gameState.getNextTribbleInSequence());
    }
    public Set<String> getPlayers() { return _allPlayers; }

}
