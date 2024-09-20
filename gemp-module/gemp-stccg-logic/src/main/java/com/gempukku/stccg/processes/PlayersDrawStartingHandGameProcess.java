package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

public class PlayersDrawStartingHandGameProcess extends GameProcess {
    private final String _firstPlayer;
    private GameProcess _followingGameProcess;
    private final DefaultGame _game;

    public PlayersDrawStartingHandGameProcess(String firstPlayer, DefaultGame game) {
        _firstPlayer = firstPlayer;
        _game = game;
    }

    @Override
    public void process() {

        GameState gameState = _game.getGameState();
        for (String playerId : gameState.getPlayerOrder().getAllPlayers()) {
            gameState.shuffleDeck(playerId);
            for (int i = 0; i < _game.getFormat().getHandSize(); i++)
                gameState.playerDrawsCard(playerId);
        }
        _followingGameProcess = new PlayersDrawStartingHandGameProcess(_firstPlayer, _game);
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
