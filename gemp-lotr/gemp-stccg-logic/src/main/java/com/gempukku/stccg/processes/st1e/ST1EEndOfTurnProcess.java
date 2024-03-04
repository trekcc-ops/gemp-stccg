package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.BetweenTurnsProcess;
import com.gempukku.stccg.processes.GameProcess;

public class ST1EEndOfTurnProcess extends ST1EGameProcess {
    private final String _playerId;
    ST1EEndOfTurnProcess(String playerId, ST1EGame game) {
        super(game);
        _playerId = playerId;
    }
    @Override
    public void process() {
//        _game.getGameState().sendMessage("DEBUG: End of turn phase.");
        for (PhysicalCard card : Filters.filterActive(_game, Filters.ship)) {
            ((PhysicalShipCard) card).restoreRange();
        }
        _game.getGameState().playerDrawsCard(_playerId);
        _game.getModifiersEnvironment().signalEndOfTurn();
        _game.getActionsEnvironment().signalEndOfTurn();
    }

    @Override
    public GameProcess getNextProcess() {
        return new BetweenTurnsProcess(_game, new ST1EStartOfTurnGameProcess(_game)); }
}
