package com.gempukku.stccg.actions.movecard;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseCardsOnTableEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.google.common.collect.Iterables;

import java.util.*;

public class FlyShipAction extends AbstractCostToEffectAction {
    private final PhysicalShipCard _flyingCard;
    private boolean _destinationChosen, _cardMoved;
    private PhysicalCard _destination;
    private final Collection<PhysicalCard> _destinationOptions;

    public FlyShipAction(Player player, PhysicalShipCard flyingCard) {
        super(player, "Fly", ActionType.MOVE_CARDS);
        _flyingCard = flyingCard;
        _destinationOptions = new LinkedList<>();
            // TODO - Include non-mission cards in location options (like Gaps in Normal Space)
        List<ST1ELocation> allLocations = _flyingCard.getGame().getGameState().getSpacelineLocations();
        ST1ELocation _currentLocation = _flyingCard.getLocation();
                // TODO - Does not include logic for inter-quadrant flying (e.g. through wormholes)
        for (ST1ELocation location : allLocations) {
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
    }

    private Effect chooseDestinationEffect() {
        DefaultGame game = _destinationOptions.stream().toList().getFirst().getGame();
        Player performingPlayer = game.getPlayer(_performingPlayerId);
        return new ChooseCardsOnTableEffect(this, performingPlayer,
                "Choose destination", _destinationOptions) {
            @Override
            protected void cardsSelected(Collection<PhysicalCard> cards) {
                _destinationChosen = true;
                _destination = Iterables.getOnlyElement(cards);
            }
        };

    }
    @Override
    public PhysicalCard getCardForActionSelection() { return _flyingCard; }

    @Override
    public PhysicalCard getActionSource() { return _flyingCard; }

    @Override
    public Effect nextEffect(DefaultGame cardGame) throws InvalidGameLogicException {
//        if (!isAnyCostFailed()) {

        Effect cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_destinationChosen) {
            appendTargeting(chooseDestinationEffect());
            return getNextCost();
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

        return getNextEffect();
    }

    @Override
    public boolean canBeInitiated(DefaultGame cardGame) { return !_flyingCard.isDocked() && !_destinationOptions.isEmpty(); }

}