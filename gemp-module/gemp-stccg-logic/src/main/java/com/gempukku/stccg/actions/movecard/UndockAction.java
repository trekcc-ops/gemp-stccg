package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class UndockAction extends ActionyAction {
    private final PhysicalShipCard _cardToMove;

    public UndockAction(Player player, PhysicalShipCard cardUndocking) {
        super(player, "Undock", ActionType.MOVE_CARDS);
        _cardToMove = cardUndocking;
    }

    @Override
    public PhysicalCard getCardForActionSelection() { return _cardToMove; }
    @Override
    public PhysicalCard getActionSource() { return _cardToMove; }

    @Override
    public Action nextAction(DefaultGame cardGame) {
//        if (!isAnyCostFailed()) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_wasCarriedOut) {
            _cardToMove.undockFromFacility();
            _wasCarriedOut = true;
        }

        return getNextAction();
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return _cardToMove.isDocked(); }

}