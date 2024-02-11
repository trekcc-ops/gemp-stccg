package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

public class PlayersDrawStartingHandGameProcess implements GameProcess {
    private final String _firstPlayer;
    private GameProcess _followingGameProcess;

    public PlayersDrawStartingHandGameProcess(String firstPlayer) {
        _firstPlayer = firstPlayer;
    }

    @Override
    public void process(DefaultGame game) {

        GameState gameState = game.getGameState();
        for (String player : gameState.getPlayerOrder().getAllPlayers()) {
            gameState.shuffleDeck(player);
            for (int i = 0; i < game.getFormat().getHandSize(); i++)
                gameState.playerDrawsCard(game, player);
        }
        if (game.getFormat().hasMulliganRule())
            _followingGameProcess = new MulliganProcess(game.getGameState().getPlayerOrder().getClockwisePlayOrder(_firstPlayer, false));
        else
            _followingGameProcess = new StartOfTurnGameProcess();
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
