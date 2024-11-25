package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

public class ST1EEndOfTurnProcess extends ST1EGameProcess {

    public ST1EEndOfTurnProcess(ST1EGame game) {
        super(game);
    }

    @Override
    public void process() {
        String playerId = _game.getCurrentPlayerId();
        for (PhysicalCard card : Filters.filterActive(_game, Filters.ship))
            ((PhysicalShipCard) card).restoreRange();
        _game.getGameState().playerDrawsCard(playerId);
        _game.getGameState().sendMessage(playerId + " drew a card to end their turn");
        _game.getModifiersEnvironment().signalEndOfTurn();
        _game.getActionsEnvironment().signalEndOfTurn();
    }

    @Override
    public GameProcess getNextProcess() {
        _game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
        String playerId = _game.getGameState().getCurrentPlayerId();
        ActionOrder actionOrder = _game.getGameState().getPlayerOrder().getClockwisePlayOrder(playerId, false);
        actionOrder.getNextPlayer();

        String nextPlayer = actionOrder.getNextPlayer();
        _game.getGameState().startPlayerTurn(nextPlayer);
        return new ST1EStartOfTurnGameProcess(_game); }
}