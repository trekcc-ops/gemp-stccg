package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;

public class DockAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalShipCard _cardToDock;
    private boolean _targetChosen;
    private boolean _cardDocked;
    private FacilityCard _dockingTarget;
    private final Collection<FacilityCard> _dockingTargetOptions;
    private final SelectVisibleCardAction _selectAction;

    public DockAction(Player player, PhysicalShipCard cardToDock) {
        super(player, "Dock", ActionType.MOVE_CARDS);
        _cardToDock = cardToDock;

        _dockingTargetOptions = Filters.yourFacilitiesInPlay(player).stream()
                .filter(card -> {
                    try {
                        return card.isCompatibleWith(_cardToDock) && card.getLocation() == _cardToDock.getLocation();
                    } catch (InvalidGameLogicException e) {
                        player.getGame().sendErrorMessage(e);
                        return false;
                    }
                })
                .toList();
        _selectAction = new SelectVisibleCardAction(player,
                "Choose facility to dock at", _dockingTargetOptions);
    }

    @Override
    public PhysicalCard getCardForActionSelection() { return _cardToDock; }
    @Override
    public PhysicalCard getPerformingCard() { return _cardToDock; }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
//        if (!isAnyCostFailed()) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_targetChosen) {
            if (!_selectAction.wasCarriedOut()) {
                appendTargeting(_selectAction);
                return getNextCost();
            } else {
                _targetChosen = true;
                _dockingTarget = (FacilityCard) _selectAction.getSelectedCard();
            }
        }

        if (!_cardDocked) {
            _cardDocked = true;
            _cardToDock.dockAtFacility(_dockingTarget);
            _cardToDock.getGame().getGameState().transferCard(_cardToDock, _dockingTarget);
        }

        return getNextAction();
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardToDock.isDocked() && !_dockingTargetOptions.isEmpty();
    }

}