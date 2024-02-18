package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.ST1EGame;

public class ST1EEndOfTurnProcess extends ST1EGameProcess {
    private final String _playerId;
    ST1EEndOfTurnProcess(String playerId, ST1EGame game) {
        super(game);
        _playerId = playerId;
    }
    @Override
    public void process() {
        _game.getGameState().sendMessage("DEBUG: End of turn phase.");
        _game.getGameState().playerDrawsCard(_playerId);
    }

    @Override
    public GameProcess getNextProcess() {
        return new BetweenTurnsProcess(_game, new ST1EStartOfTurnGameProcess(_game)); }
}
