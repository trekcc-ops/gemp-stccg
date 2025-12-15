package com.gempukku.stccg.actions.movecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FlyShipAction extends ActionyAction implements TopLevelSelectableAction {
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final ShipCard _flyingCard;
    private final Collection<PhysicalCard> _destinationOptions;
    private final ActionCardResolver _destinationTargetResolver;

    public FlyShipAction(Player player, ShipCard flyingCard, ST1EGame cardGame)
            throws InvalidGameLogicException {
        super(cardGame, player, ActionType.FLY_SHIP);
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
        _destinationTargetResolver = new SelectCardsResolver(
                new SelectVisibleCardAction(cardGame, _performingPlayerId,
                        "Choose destination", _destinationOptions));
        _cardTargets.add(_destinationTargetResolver);
    }

    @Override
    public PhysicalCard getPerformingCard() { return _flyingCard; }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            Collection<PhysicalCard> destinationCards = _destinationTargetResolver.getCards(cardGame);
            if (destinationCards.size() == 1 && cardGame instanceof ST1EGame stGame) {
                PhysicalCard destinationCard = Iterables.getOnlyElement(destinationCards);
                GameLocation originLocation = _flyingCard.getGameLocation(stGame);
                GameLocation destinationLocation = destinationCard.getGameLocation(stGame);
                int rangeNeeded = originLocation.getDistanceToLocation(stGame, destinationLocation, performingPlayer);
                setAsSuccessful();
                _flyingCard.useRange(rangeNeeded);
                _flyingCard.setLocation(cardGame, destinationLocation);
            } else {
                throw new InvalidGameLogicException("Uanble to resolve flying ship action");
            }
        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_flyingCard.isDocked() && !_destinationOptions.isEmpty();
    }

}