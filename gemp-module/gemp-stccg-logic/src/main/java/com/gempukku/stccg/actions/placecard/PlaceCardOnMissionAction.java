package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;

public class PlaceCardOnMissionAction extends ActionyAction {

    private final PhysicalCard _cardBeingPlaced;
    private final MissionLocation _mission;


    public PlaceCardOnMissionAction(DefaultGame cardGame, Player performingPlayer, PhysicalCard cardBeingPlaced,
                                    MissionLocation mission) {
        super(cardGame, performingPlayer, ActionType.PLACE_CARD);
        _mission = mission;
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
        gameState.placeCardOnMission(cardGame, _cardBeingPlaced, _mission);
        for (MissionLocation location : gameState.getSpacelineLocations()) {
            if (location.getSeedCards().contains(_cardBeingPlaced)) {
                location.removeSeedCard(_cardBeingPlaced);
            }
        }
        cardGame.sendMessage(_cardBeingPlaced.getTitle() + " was placed on " + _mission.getLocationName());
        return getNextAction();
    }
}