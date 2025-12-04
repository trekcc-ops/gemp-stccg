package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;

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
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
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
            gameState.placeCardOnMission(cardGame, _cardBeingPlaced, mission);

            for (MissionLocation spacelineLocation : gameState.getSpacelineLocations()) {
                if (spacelineLocation.getSeedCards().contains(_cardBeingPlaced)) {
                    spacelineLocation.removeSeedCard(_cardBeingPlaced);
                }
            }
            setAsSuccessful();
        } else {
            setAsFailed();
            throw new InvalidGameLogicException("Unable to place card on null location");
        }
        return getNextAction();
    }
}