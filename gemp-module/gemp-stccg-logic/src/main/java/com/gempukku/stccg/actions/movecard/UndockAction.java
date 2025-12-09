package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameOperationException;

public class UndockAction extends ActionyAction implements TopLevelSelectableAction {
    private final ShipCard _performingCard;

    public UndockAction(DefaultGame cardGame, String performingPlayerName, ShipCard cardUndocking) {
        super(cardGame, performingPlayerName, "Undock", ActionType.UNDOCK_SHIP);
        _performingCard = cardUndocking;
    }


    @Override
    public PhysicalCard getPerformingCard() { return _performingCard; }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameOperationException {
//        if (!isAnyCostFailed()) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (isInProgress()) {
            _performingCard.undockFromFacility();
            setAsSuccessful();
        }

        return getNextAction();
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return _performingCard.isDocked(); }

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    public ShipCard getCardToMove() { return _performingCard; }

}