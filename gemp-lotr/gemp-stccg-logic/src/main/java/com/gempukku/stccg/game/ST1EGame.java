package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.gamestate.GameStateListener;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.UserFeedback;
import com.gempukku.stccg.processes.ST1EGameProcess;
import com.gempukku.stccg.processes.ST1EPlayerOrderProcess;
import com.gempukku.stccg.processes.TurnProcedure;
import com.gempukku.stccg.rules.ST1ERuleSet;

import java.util.Map;
import java.util.Set;

public class ST1EGame extends DefaultGame {
    private final ST1EGameState _gameState;
    private final TurnProcedure _turnProcedure;
    private final ST1EGame _thisGame;

    public ST1EGame(GameFormat format, Map<String, CardDeck> decks, UserFeedback userFeedback,
                    final CardBlueprintLibrary library) {
        super(format, decks, userFeedback, library);
        _thisGame = this;

        _gameState = new ST1EGameState(_allPlayers, decks, library, _format, this);
        new ST1ERuleSet(_actionsEnvironment, _modifiersLogic).applyRuleSet();

        _gameState.createPhysicalCards();
        _turnProcedure = new TurnProcedure(this, _allPlayers, userFeedback, _actionsEnvironment,
                _gameState::init) {
            @Override
            protected ST1EGameProcess setFirstGameProcess(Set<String> players,
                                                          PlayerOrderFeedback playerOrderFeedback) {
                return new ST1EPlayerOrderProcess(players, playerOrderFeedback, _thisGame);
            }
        };
    }

    @Override
    public ST1EGameState getGameState() {
        return _gameState;
    }

    public TurnProcedure getTurnProcedure() { return _turnProcedure; }
    @Override
    public void addGameStateListener(String playerId, GameStateListener gameStateListener) {
        getGameState().addGameStateListener(playerId, gameStateListener, _turnProcedure.getGameStats());
    }

}
