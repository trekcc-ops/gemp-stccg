package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FlyShipAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalShipCard _flyingCard;
    private boolean _destinationChosen, _cardMoved;
    private PhysicalCard _destination;
    private final Collection<PhysicalCard> _destinationOptions;
    private final SelectVisibleCardAction _selectAction;

    public FlyShipAction(Player player, PhysicalShipCard flyingCard) throws InvalidGameLogicException {
        super(player, "Fly", ActionType.MOVE_CARDS);
        _flyingCard = flyingCard;
        _destinationOptions = new LinkedList<>();
            // TODO - Include non-mission cards in location options (like Gaps in Normal Space)
        List<MissionLocation> allLocations = _flyingCard.getGame().getGameState().getSpacelineLocations();
        MissionLocation _currentLocation = _flyingCard.getLocation();
                // TODO - Does not include logic for inter-quadrant flying (e.g. through wormholes)
        for (MissionLocation location : allLocations) {
            if (location.getQuadrant() == _currentLocation.getQuadrant() && location != _currentLocation) {
                try {
                    int rangeNeeded = _currentLocation.getDistanceToLocation(location, player);
                    if (rangeNeeded <= _flyingCard.getRangeAvailable()) {
                        PhysicalCard destination = location.getMissionForPlayer(player.getPlayerId());
                        _destinationOptions.add(destination);
                        _destinationOptions.add(location.getMissionForPlayer(player.getPlayerId()));
                    }
                } catch(InvalidGameLogicException exp) {
                    player.getGame().sendMessage(exp.getMessage());
                }
            }
        }
        _selectAction =
                new SelectVisibleCardAction(player, "Choose destination", _destinationOptions);
    }

    @Override
    public PhysicalCard getCardForActionSelection() { return _flyingCard; }

    @Override
    public PhysicalCard getPerformingCard() { return _flyingCard; }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
//        if (!isAnyCostFailed()) {

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_destinationChosen) {
            if (_selectAction.wasCarriedOut()) {
                _destinationChosen = true;
                _destination = _selectAction.getSelectedCard();
            } else {
                appendTargeting(_selectAction);
                return getNextCost();
            }
        }

        if (!_cardMoved) {
            DefaultGame game = _flyingCard.getGame();
            int rangeNeeded =
                    _flyingCard.getLocation().getDistanceToLocation(_destination.getLocation(),
                            game.getPlayer(_performingPlayerId));
            _cardMoved = true;
            _flyingCard.useRange(rangeNeeded);
            _flyingCard.setLocation(_destination.getLocation());
            _flyingCard.getGame().getGameState().moveCard(_flyingCard);
            _flyingCard.getGame().sendMessage(
                    _flyingCard.getCardLink() + " flew to " + _destination.getLocation().getLocationName() +
                            " (using " + rangeNeeded + " RANGE)"
            );
        }

        return getNextAction();
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_flyingCard.isDocked() && !_destinationOptions.isEmpty();
    }

}