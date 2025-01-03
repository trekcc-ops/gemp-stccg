package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Arrays;

public class PlaceCardOnTopOfDrawDeckAction extends ActionyAction {

    private final PhysicalCard _cardBeingPlaced;
    private final PhysicalCard _causingCard;


    public PlaceCardOnTopOfDrawDeckAction(Player performingPlayer, PhysicalCard cardBeingPlaced,
                                          PhysicalCard causingCard) {
        super(performingPlayer, ActionType.PLACE_CARD);
        _cardBeingPlaced = cardBeingPlaced;
        _causingCard = causingCard;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _causingCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _causingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZone(_cardBeingPlaced.getOwnerName(), Arrays.asList(_cardBeingPlaced));
        gameState.sendMessage(_performingPlayerId + " placed " + _cardBeingPlaced + " on top of their draw deck");
        gameState.addCardToZone(_cardBeingPlaced, Zone.DRAW_DECK, EndOfPile.TOP);
        return getNextAction();
    }
}