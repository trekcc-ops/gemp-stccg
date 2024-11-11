package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class UndockAction extends ActionyAction {
    private final PhysicalShipCard _cardToUndock;
    private boolean _cardUndocked;

    public UndockAction(Player player, PhysicalShipCard cardUndocking) {
        super(player, "Undock", ActionType.MOVE_CARDS);
        _cardToUndock = cardUndocking;
    }

    @Override
    public PhysicalCard getCardForActionSelection() { return _cardToUndock; }
    @Override
    public PhysicalCard getActionSource() { return _cardToUndock; }

    @Override
    public Action nextAction(DefaultGame cardGame) {
//        if (!isAnyCostFailed()) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_cardUndocked) {
            _cardUndocked = true;
            _cardToUndock.undockFromFacility();
        }

        return getNextAction();
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return _cardToUndock.isDocked(); }

}