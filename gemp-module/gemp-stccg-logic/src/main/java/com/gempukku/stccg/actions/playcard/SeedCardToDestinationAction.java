package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.targetresolver.EnterPlayAtDestinationResolver;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeedCardToDestinationAction extends SeedCardAction {
    private final EnterPlayAtDestinationResolver _targetResolver;
    private final boolean _onPlanet;

    public SeedCardToDestinationAction(DefaultGame cardGame, String performingPlayerName,
                                       Collection<PhysicalCard> seedableCards,
                                       Collection<PhysicalCard> destinationOptions,
                                       PhysicalCard performingCard) {
        super(cardGame, performingCard, null);
        Map<PhysicalCard, Collection<PhysicalCard>> destinationMap = new HashMap<>();
        for (PhysicalCard card : seedableCards) {
            destinationMap.put(card, destinationOptions);
        }
        _targetResolver = new EnterPlayAtDestinationResolver(performingPlayerName, destinationMap);
        _cardTargets.add(_targetResolver);
        _onPlanet = false;
    }

    public SeedCardToDestinationAction(DefaultGame cardGame, String performingPlayerName,
                                       Collection<PhysicalCard> seedableCards,
                                       Collection<PhysicalCard> destinationOptions,
                                       PhysicalCard performingCard, boolean onPlanet) {
        super(cardGame, performingCard, null);
        Map<PhysicalCard, Collection<PhysicalCard>> destinationMap = new HashMap<>();
        for (PhysicalCard card : seedableCards) {
            destinationMap.put(card, destinationOptions);
        }
        _targetResolver = new EnterPlayAtDestinationResolver(performingPlayerName, destinationMap);
        _cardTargets.add(_targetResolver);
        _onPlanet = onPlanet;
    }


    public void processEffect(DefaultGame cardGame) {
        if (cardGame instanceof ST1EGame stGame) {
            PhysicalCard cardEnteringPlay = _targetResolver.getCardEnteringPlay();
            GameState gameState = stGame.getGameState();
            PhysicalCard destination = _targetResolver.getDestination();
            cardGame.removeCardsFromZone(List.of(cardEnteringPlay));
            gameState.addCardToZone(cardGame, cardEnteringPlay, Zone.AT_LOCATION);
            if (destination instanceof CardWithCrew && cardEnteringPlay instanceof ReportableCard reportable) {
                // if reporting to a ship or facility
                if (reportable.getCardType() != CardType.SHIP) {
                    reportable.setAsAboard(destination);
                }
                if (reportable instanceof ShipCard ship && destination instanceof FacilityCard facility) {
                    ship.setAsDockedAt(facility);
                }
            } else if (cardEnteringPlay instanceof CardWithCrew cardWithCrew && !_onPlanet) {
                // if reporting a ship or facility in space at a location
                cardWithCrew.setAsInSpaceAtLocation(destination);
            } else if (cardEnteringPlay instanceof ReportableCard reportable) {
                // if reporting another reportable to a location
                reportable.setAsOnPlanet(destination);
                if (destination instanceof MissionCard missionDestination &&
                        missionDestination.getGameLocation(stGame) instanceof MissionLocation missionLocation) {
                    stGame.getGameState().addCardToEligibleAwayTeam(stGame, reportable, missionLocation);
                }
            } else {
                if (_onPlanet) {
                    cardEnteringPlay.setAsOnPlanet(destination);
                } else {
                    cardEnteringPlay.setAsAtop(destination);
                }
            }
            saveResult(new PlayCardResult(cardGame, this, cardEnteringPlay), cardGame);
            setAsSuccessful();
        } else {
            cardGame.sendErrorMessage("Unable to process seed card action");
            setAsFailed();
        }
    }

    public Collection<PhysicalCard> getSeedableOptions() {
        return _targetResolver.getSelectableOptions();
    }

}