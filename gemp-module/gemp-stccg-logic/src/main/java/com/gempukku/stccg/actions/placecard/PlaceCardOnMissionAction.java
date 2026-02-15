package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.List;

public class PlaceCardOnMissionAction extends ActionyAction {

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _cardBeingPlaced;
    private final int _locationId;


    public PlaceCardOnMissionAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard cardBeingPlaced,
                                    MissionLocation mission) {
        super(cardGame, performingPlayerName, ActionType.PLACE_CARD_ON_MISSION);
        _locationId = mission.getLocationId();
        _cardBeingPlaced = cardBeingPlaced;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public void processEffect(DefaultGame cardGame) {
        try {
            ST1EGame stGame = null;
            if (cardGame instanceof ST1EGame) {
                stGame = (ST1EGame) cardGame;
            }
            if (stGame == null) {
                throw new InvalidGameLogicException("Unable to place card on a mission in a non-1E game");
            }
            ST1EGameState gameState = stGame.getGameState();
            GameLocation location = gameState.getLocationById(_locationId);
            if (location instanceof MissionLocation mission) {
                gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(_cardBeingPlaced));
                _cardBeingPlaced.setPlacedOnMission(true);
                _cardBeingPlaced.setLocation(cardGame, mission);
                gameState.addCardToZone(cardGame, _cardBeingPlaced, Zone.AT_LOCATION, _actionContext);

                for (GameLocation spacelineLocation : gameState.getOrderedSpacelineLocations()) {
                    if (spacelineLocation instanceof MissionLocation missionLoc &&
                            missionLoc.getSeedCards().contains(_cardBeingPlaced)) {
                        missionLoc.removeSeedCard(_cardBeingPlaced);
                    }
                }
                setAsSuccessful();
            } else {
                setAsFailed();
                throw new InvalidGameLogicException("Unable to place card on null location");
            }
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }
}