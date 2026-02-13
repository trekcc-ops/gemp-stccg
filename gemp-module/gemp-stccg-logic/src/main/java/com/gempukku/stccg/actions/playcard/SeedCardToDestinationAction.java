package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.targetresolver.SeedCardToDestinationResolver;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.List;

public class SeedCardToDestinationAction extends SeedCardAction {
    private final SeedCardToDestinationResolver _targetResolver;

    public SeedCardToDestinationAction(DefaultGame cardGame, String performingPlayerName,
                                       Collection<PhysicalCard> seedableCards,
                                       Collection<PhysicalCard> destinationOptions,
                                       PhysicalCard performingCard) {
        super(cardGame, performingCard, null);
        _targetResolver = new SeedCardToDestinationResolver(performingPlayerName, seedableCards, destinationOptions);
        _cardTargets.add(_targetResolver);
    }


    public void processEffect(DefaultGame cardGame) {
        if (cardGame instanceof ST1EGame stGame &&
                _targetResolver.getCardToSeed() instanceof ReportableCard reportable) {
            GameState gameState = stGame.getGameState();
            PhysicalCard destination = _targetResolver.getDestination();
            cardGame.removeCardsFromZone(List.of(reportable));
            setAsSuccessful();
            if (destination instanceof CardWithCrew cardWithCrew) {
                // if reporting to a ship or facility
                reportable.attachTo(destination);
                gameState.addCardToZone(cardGame, reportable, Zone.ATTACHED, _actionContext);
                if (reportable instanceof ShipCard ship && destination instanceof FacilityCard facility) {
                    ship.dockAtFacility(facility);
                }
            } else if (reportable.getCardType() == CardType.SHIP) {
                // if reporting a ship in space at a location
                gameState.addCardToZone(cardGame, reportable, Zone.AT_LOCATION, _actionContext);
            } else {
                // if reporting another reportable to a location
                gameState.addCardToZone(cardGame, reportable, Zone.ATTACHED, _actionContext);
                reportable.attachTo(destination);
                reportable.setLocationId(cardGame, destination.getLocationId());
                if (destination instanceof MissionCard missionDestination &&
                        missionDestination.getGameLocation(stGame) instanceof MissionLocation missionLocation) {
                    stGame.getGameState().addCardToEligibleAwayTeam(stGame, reportable, missionLocation);
                }
            }
        } else {
            cardGame.sendErrorMessage("Unable to process seed card action");
            setAsFailed();
        }
    }

    public Collection<PhysicalCard> getSeedableOptions() {
        return _targetResolver.getSelectableOptions();
    }

}