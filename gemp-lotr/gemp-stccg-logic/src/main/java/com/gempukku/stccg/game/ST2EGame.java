package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.*;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.ST1EGameProcess;
import com.gempukku.stccg.processes.ST1EPlayerOrderProcess;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.RuleSet;

import java.util.Map;
import java.util.Set;

public class ST2EGame extends DefaultGame {
    private final ST2EGameState _gameState;
    private final TurnProcedure _turnProcedure;
    private final ST2EGame _thisGame;

    public ST2EGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                    final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);
        _thisGame = this;

        _gameState = new ST2EGameState(_allPlayers, decks, library, _format, this);
        new RuleSet(_actionsEnvironment, _modifiersLogic).applyRuleSet();

        _turnProcedure = new TurnProcedure(this, _allPlayers, userFeedback, _actionsEnvironment,
                _gameState::init) {
            @Override
            protected GameProcess setFirstGameProcess(Set<String> players,
                                                      PlayerOrderFeedback playerOrderFeedback) {
                return _thisGame.getFormat().getStartingGameProcess(players, playerOrderFeedback, _thisGame);
            }
        };
    }


    @Override
    public ST2EGameState getGameState() {
        return _gameState;
    }
    public TurnProcedure getTurnProcedure() { return _turnProcedure; }
    @Override
    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        getGameState().addGameStateListener(playerId, gameStateListener, _turnProcedure.getGameStats());
    }
}
