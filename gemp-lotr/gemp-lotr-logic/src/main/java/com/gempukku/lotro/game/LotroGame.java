package com.gempukku.lotro.game;

import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.actions.ActionsEnvironment;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.adventure.Adventure;
import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.LotroDeck;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.gamestate.GameState;
import com.gempukku.lotro.gamestate.UserFeedback;
import com.gempukku.lotro.modifiers.ModifiersEnvironment;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.modifiers.ModifiersQuerying;
import com.gempukku.lotro.processes.TurnProcedure;
import com.gempukku.lotro.rules.RuleSet;
import com.gempukku.lotro.rules.lotronly.CharacterDeathRule;

import java.util.*;

public class LotroGame implements DefaultGame {

    private final GameState _gameState;
    private final ModifiersLogic _modifiersLogic = new ModifiersLogic();
    private final DefaultActionsEnvironment _actionsEnvironment;
    private final UserFeedback _userFeedback;
    private final TurnProcedure _turnProcedure;
    private final boolean _cancelled = false;
    private boolean _finished;

    private final LotroFormat _format;

    private final Set<String> _allPlayers;
    private final Map<String, Set<Phase>> _autoPassConfiguration = new HashMap<>();

    private String _winnerPlayerId;
    private final Map<String, String> _losers = new HashMap<>();

    private final Set<GameResultListener> _gameResultListeners = new HashSet<>();

    private final CardBlueprintLibrary _library;

    public LotroGame(LotroFormat format, Map<String, LotroDeck> decks, UserFeedback userFeedback, final CardBlueprintLibrary library) {
        _library = library;
        Adventure _adventure = format.getAdventure();
        _format = format;
        Stack<Action> _actionStack = new Stack<>();

        _allPlayers = decks.keySet();

        _actionsEnvironment = new DefaultActionsEnvironment(this, _actionStack);

        final Map<String, List<String>> cards = new HashMap<>();
        final Map<String, String> ringBearers = new HashMap<>();
        final Map<String, String> rings = new HashMap<>();
        for (String playerId : decks.keySet()) {
            List<String> deck = new LinkedList<>();

            LotroDeck lotroDeck = decks.get(playerId);
            deck.addAll(lotroDeck.getSites());
            deck.addAll(lotroDeck.getDrawDeckCards());

            cards.put(playerId, deck);
            ringBearers.put(playerId, lotroDeck.getRingBearer());
            if (lotroDeck.getRing() != null)
                rings.put(playerId, lotroDeck.getRing());
        }

        _gameState = new GameState();

        CharacterDeathRule characterDeathRule = new CharacterDeathRule(_actionsEnvironment);
        characterDeathRule.applyRule();

        _turnProcedure = new TurnProcedure(this, decks.keySet(), userFeedback, _actionStack,
                (playerOrder, firstPlayer) -> _gameState.init(playerOrder, firstPlayer, cards, ringBearers, rings, library, format));
        _userFeedback = userFeedback;

        RuleSet ruleSet = new RuleSet(_actionsEnvironment, _modifiersLogic);
        ruleSet.applyRuleSet();

        _adventure.applyAdventureRules(this, _actionsEnvironment, _modifiersLogic);
    }


    @Override
    public boolean shouldAutoPass(String playerId, Phase phase) {
        final Set<Phase> passablePhases = _autoPassConfiguration.get(playerId);
        if (passablePhases == null)
            return false;
        return passablePhases.contains(phase);
    }

    @Override
    public boolean isSolo() {
        return _allPlayers.size() == 1;
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

    public GameState getGameState() {
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

    // Dummy function. LotroGame will eventually be deprecated.
    public boolean checkPlayRequirements(LotroPhysicalCard card) { return true; }
    public Set<String> getPlayers() { return _allPlayers; }
}
