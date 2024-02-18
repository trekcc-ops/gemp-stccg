package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

public class PlayersDrawStartingHandGameProcess extends GameProcess {
    private final String _firstPlayer;
    private GameProcess _followingGameProcess;
    private DefaultGame _game;

    public PlayersDrawStartingHandGameProcess(String firstPlayer, DefaultGame game) {
        _firstPlayer = firstPlayer;
        _game = game;
    }

    @Override
    public void process() {

        GameState gameState = _game.getGameState();
        for (String player : gameState.getPlayerOrder().getAllPlayers()) {
            gameState.shuffleDeck(player);
            for (int i = 0; i < _game.getFormat().getHandSize(); i++)
                gameState.playerDrawsCard(_game, player);
        }
        if (_game.getFormat().hasMulliganRule())
            _followingGameProcess = new MulliganProcess(
                    _game.getGameState().getPlayerOrder().getClockwisePlayOrder(_firstPlayer, false),
                    _game
            );
        else
            _followingGameProcess = new PlayersDrawStartingHandGameProcess(_firstPlayer, _game);
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
