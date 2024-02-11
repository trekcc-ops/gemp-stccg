package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.ST1EGame;

public class ST1EEndOfTurnProcess implements GameProcess<ST1EGame> {
    private final String _playerId;
    ST1EEndOfTurnProcess(String playerId) {
        _playerId = playerId;
    }
    @Override
    public void process(ST1EGame game) {
        game.getGameState().sendMessage("DEBUG: End of turn phase.");
        game.getGameState().playerDrawsCard(_playerId);
    }

    @Override
    public GameProcess getNextProcess() {
        return new BetweenTurnsProcess(); }
}
