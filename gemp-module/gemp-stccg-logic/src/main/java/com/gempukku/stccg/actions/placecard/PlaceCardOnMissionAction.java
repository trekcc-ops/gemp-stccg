package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;

public class PlaceCardOnMissionAction extends ActionyAction {

    private final PhysicalCard _cardBeingPlaced;
    private final PhysicalCard _performingCard;
    private final MissionLocation _mission;


    public PlaceCardOnMissionAction(Player performingPlayer, PhysicalCard performingCard,
                                    PhysicalCard cardBeingPlaced, MissionLocation mission) {
        super(performingPlayer, ActionType.PLACE_CARD);
        _mission = mission;
        _cardBeingPlaced = cardBeingPlaced;
        _performingCard = performingCard;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _performingCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        GameState gameState = cardGame.getGameState();
        gameState.placeCardOnMission(_cardBeingPlaced, _mission);
        gameState.sendMessage(_cardBeingPlaced.getTitle() + " was placed on " + _mission.getLocationName());
        return getNextAction();
    }
}