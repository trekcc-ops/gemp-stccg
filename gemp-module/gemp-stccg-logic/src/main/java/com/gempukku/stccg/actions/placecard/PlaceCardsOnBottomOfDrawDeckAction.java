package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PlaceCardsOnBottomOfDrawDeckAction extends ActionyAction {

    private SelectCardsAction _selectionAction;
    private final PhysicalCard _causingCard;
    private Collection<PhysicalCard> _cardsToPlace;
    private enum Progress { cardsSelected }


    public PlaceCardsOnBottomOfDrawDeckAction(Player performingPlayer, SelectCardsAction selectionAction,
                                              PhysicalCard causingCard) {
        super(performingPlayer, ActionType.PLACE_CARD, Progress.values());
        _selectionAction = selectionAction;
        _causingCard = causingCard;
    }

    public PlaceCardsOnBottomOfDrawDeckAction(Player performingPlayer, Collection<PhysicalCard> cardsToPlace,
                                              PhysicalCard causingCard) {
        super(performingPlayer, ActionType.PLACE_CARD);
        _causingCard = causingCard;
        _cardsToPlace = cardsToPlace;
    }


    @Override
    public PhysicalCard getPerformingCard() {
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
        if (!getProgress(Progress.cardsSelected)) {
            if (_selectionAction != null && !_selectionAction.wasCarriedOut()) {
                return _selectionAction;
            } else if (_selectionAction != null) {
                _cardsToPlace = _selectionAction.getSelectedCards();
                setProgress(Progress.cardsSelected);
            } else {
                throw new InvalidGameLogicException("Unable to identify cards to place on bottom of deck");
            }
        }

        for (PhysicalCard card : _cardsToPlace) {
            GameState gameState = cardGame.getGameState();
            gameState.removeCardsFromZone(card.getOwnerName(), List.of(card));
            gameState.sendMessage(_performingPlayerId + " placed " + card + " beneath their draw deck");
            gameState.addCardToZone(card, Zone.DRAW_DECK, EndOfPile.BOTTOM);
        }
        return getNextAction();
    }
}