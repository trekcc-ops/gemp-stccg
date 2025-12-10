package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;

public class DockAction extends ActionyAction implements TopLevelSelectableAction {
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final ShipCard _cardToDock;
    private boolean _targetChosen;
    private boolean _cardDocked;
    private FacilityCard _dockingTarget;
    private final Collection<FacilityCard> _dockingTargetOptions;
    private SelectVisibleCardAction _selectAction;

    public DockAction(Player player, ShipCard cardToDock, ST1EGame cardGame) {
        super(cardGame, player, "Dock", ActionType.DOCK_SHIP);
        _cardToDock = cardToDock;

        _dockingTargetOptions = Filters.yourFacilitiesInPlay(cardGame, player).stream()
                .filter(card -> card.isCompatibleWith(cardGame, _cardToDock) &&
                        card.isAtSameLocationAsCard(_cardToDock))
                .toList();
    }

    @Override
    public PhysicalCard getPerformingCard() { return _cardToDock; }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException, InvalidGameOperationException {
//        if (!isAnyCostFailed()) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_targetChosen) {
            if (_selectAction == null) {
                _selectAction = new SelectVisibleCardAction(cardGame, _performingPlayerId,
                        "Choose facility to dock at", _dockingTargetOptions);
            }
            if (!_selectAction.wasCarriedOut()) {
                return(_selectAction);
            } else {
                _targetChosen = true;
                _dockingTarget = (FacilityCard) _selectAction.getSelectedCard();
            }
        }

        if (!_cardDocked) {
            _cardDocked = true;
            setAsSuccessful();
            _cardToDock.dockAtFacility(_dockingTarget);
            _cardToDock.setZone(Zone.ATTACHED);
            _cardToDock.attachTo(_dockingTarget);
        }

        return getNextAction();
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardToDock.isDocked() && !_dockingTargetOptions.isEmpty();
    }

}