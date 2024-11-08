package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.ST1EBetweenTurnsProcess;

import java.util.HashSet;

public class ST1EEndOfTurnProcess extends ST1EGameProcess {

    ST1EEndOfTurnProcess(ST1EGame game) {
        super(new HashSet<>(), game);
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
        return new ST1EBetweenTurnsProcess(_game); }
}