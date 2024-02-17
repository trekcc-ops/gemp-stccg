package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;

public class ST1EStartOfPlayPhaseProcess implements GameProcess<ST1EGame> {
    private final String _firstPlayer;
    private GameProcess _followingGameProcess;

    public ST1EStartOfPlayPhaseProcess(String firstPlayer) {
        _firstPlayer = firstPlayer;
    }

    @Override
    public void process(ST1EGame game) {

        ST1EGameState gameState = game.getGameState();
        for (String player : game.getPlayerIds()) {
            gameState.shuffleDeck(player);
            for (int i = 0; i < game.getFormat().getHandSize(); i++) {
                gameState.playerDrawsCard(game, player);
                gameState.sendMessage("Drew a card to hand");
            }
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
