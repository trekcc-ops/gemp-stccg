package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.choose.SelectCardsOnTableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Arrays;
import java.util.Collection;

public class PlaceCardsOnBottomOfDrawDeckAction extends ActionyAction {

    private final SelectCardsOnTableAction _selectionAction;
    private final PhysicalCard _causingCard;


    public PlaceCardsOnBottomOfDrawDeckAction(Player performingPlayer, SelectCardsOnTableAction selectionAction,
                                              PhysicalCard causingCard) {
        super(performingPlayer, ActionType.PLACE_CARD);
        _selectionAction = selectionAction;
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
        if (!_selectionAction.wasCarriedOut()) {
            return _selectionAction;
        } else {
            Collection<PhysicalCard> cardsBeingPlaced = _selectionAction.getSelectedCards();
            for (PhysicalCard card : cardsBeingPlaced) {
                GameState gameState = cardGame.getGameState();
                gameState.removeCardsFromZone(card.getOwnerName(), Arrays.asList(card));
                gameState.sendMessage(_performingPlayerId + " placed " + card + " beneath their draw deck");
                gameState.addCardToZone(card, Zone.DRAW_DECK, EndOfPile.BOTTOM);
            }
            return getNextAction();
        }
    }
}