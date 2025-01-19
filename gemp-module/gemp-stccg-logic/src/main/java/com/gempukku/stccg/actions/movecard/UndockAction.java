package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class UndockAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalShipCard _performingCard;

    public UndockAction(Player player, PhysicalShipCard cardUndocking) {
        super(player, "Undock", ActionType.MOVE_CARDS);
        _performingCard = cardUndocking;
    }

    @Override
    public int getCardIdForActionSelection() { return _performingCard.getCardId(); }
    @Override
    public PhysicalCard getPerformingCard() { return _performingCard; }

    @Override
    public Action nextAction(DefaultGame cardGame) {
//        if (!isAnyCostFailed()) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_wasCarriedOut) {
            _performingCard.undockFromFacility();
            _wasCarriedOut = true;
        }

        return getNextAction();
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return _performingCard.isDocked(); }
    public PhysicalShipCard getCardToMove() { return _performingCard; }

}