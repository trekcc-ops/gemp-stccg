package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.GameProcess;

public class ST1EStartOfPlayPhaseProcess extends ST1EGameProcess {
    private ST1EGameProcess _followingGameProcess;

    public ST1EStartOfPlayPhaseProcess(ST1EGameProcess followingProcess, ST1EGame game) {
        super(game);
        _followingGameProcess = followingProcess;
    }

    @Override
    public void process() {

        ST1EGameState gameState = _game.getGameState();
        for (String playerId : _game.getPlayerIds()) {
            gameState.shuffleDeck(playerId);
            for (int i = 0; i < _game.getFormat().getHandSize(); i++) {
                gameState.playerDrawsCard(playerId);
                gameState.sendMessage("Drew a card to hand");
            }
        }

        _followingGameProcess = new ST1EStartOfTurnGameProcess(_game);
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
