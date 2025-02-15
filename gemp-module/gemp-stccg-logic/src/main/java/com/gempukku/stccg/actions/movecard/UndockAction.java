package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.Player;

public class UndockAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalShipCard _performingCard;

    public UndockAction(Player player, PhysicalShipCard cardUndocking) {
        super(cardUndocking.getGame(), player, "Undock", ActionType.MOVE_SHIP);
        _performingCard = cardUndocking;
    }

    @Override
    public int getCardIdForActionSelection() { return _performingCard.getCardId(); }
    @Override
    public PhysicalCard getPerformingCard() { return _performingCard; }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameOperationException {
//        if (!isAnyCostFailed()) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_wasCarriedOut) {
            _performingCard.undockFromFacility();
            cardGame.getGameState().moveCard(cardGame, _performingCard);
            _wasCarriedOut = true;
            setAsSuccessful();
        }

        return getNextAction();
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return _performingCard.isDocked(); }
    public PhysicalShipCard getCardToMove() { return _performingCard; }

}