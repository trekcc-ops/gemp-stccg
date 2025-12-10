package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FlyShipAction extends ActionyAction implements TopLevelSelectableAction {
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final ShipCard _flyingCard;
    private boolean _destinationChosen, _cardMoved;

    private PhysicalCard _destination;
    private final Collection<PhysicalCard> _destinationOptions;
    private SelectVisibleCardAction _selectAction;

    public FlyShipAction(Player player, ShipCard flyingCard, ST1EGame cardGame)
            throws InvalidGameLogicException {
        super(cardGame, player, "Fly", ActionType.FLY_SHIP);
        _flyingCard = flyingCard;
        _destinationOptions = new LinkedList<>();
            // TODO - Include non-mission cards in location options (like Gaps in Normal Space)
        List<MissionLocation> allLocations = cardGame.getGameState().getSpacelineLocations();
        GameLocation currentLocation = _flyingCard.getGameLocation(cardGame);
                // TODO - Does not include logic for inter-quadrant flying (e.g. through wormholes)
        for (MissionLocation location : allLocations) {
            if (location.isInSameQuadrantAs(currentLocation) && location != currentLocation) {
                int rangeNeeded = currentLocation.getDistanceToLocation(cardGame, location, player);
                if (rangeNeeded <= _flyingCard.getRangeAvailable(cardGame)) {
                    PhysicalCard destination = location.getMissionForPlayer(player.getPlayerId());
                    _destinationOptions.add(destination);
                    _destinationOptions.add(location.getMissionForPlayer(player.getPlayerId()));
                }
            }
        }
    }

    @Override
    public PhysicalCard getPerformingCard() { return _flyingCard; }

    @Override
    public Action nextAction(DefaultGame cardGame)
            throws InvalidGameLogicException, PlayerNotFoundException, InvalidGameOperationException {
//        if (!isAnyCostFailed()) {
        ST1EGame stGame;
        if (cardGame instanceof ST1EGame)
            stGame = (ST1EGame) cardGame;
        else throw new InvalidGameLogicException("Tried to fly a ship in a non-1E game");

        Action cost = getNextCost();
        if (cost != null)
            return cost;
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        if (!_destinationChosen) {
            if (_selectAction == null) {
                _selectAction =
                        new SelectVisibleCardAction(cardGame, _performingPlayerId,
                                "Choose destination", _destinationOptions);
            }
            if (_selectAction.wasCarriedOut()) {
                _destinationChosen = true;
                _destination = _selectAction.getSelectedCard();
            } else {
                return(_selectAction);
            }
        }

        GameLocation destinationLocation = _destination.getGameLocation((ST1EGame) cardGame);

        if (!_cardMoved) {
            int rangeNeeded =
                    _flyingCard.getGameLocation((ST1EGame) cardGame).getDistanceToLocation(stGame, destinationLocation, performingPlayer);
            _cardMoved = true;
            setAsSuccessful();
            _flyingCard.useRange(rangeNeeded);
            _flyingCard.setLocation(cardGame, destinationLocation);
        }

        return getNextAction();
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_flyingCard.isDocked() && !_destinationOptions.isEmpty();
    }

}