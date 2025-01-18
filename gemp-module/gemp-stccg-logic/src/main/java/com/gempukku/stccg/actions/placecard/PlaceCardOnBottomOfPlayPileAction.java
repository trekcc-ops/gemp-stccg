package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.List;

public class PlaceCardOnBottomOfPlayPileAction extends ActionyAction {

    private PhysicalCard _cardBeingPlaced;
    private final SelectCardsAction _selectCardAction;
    private enum Progress { cardSelected, wasCarriedOut }


    public PlaceCardOnBottomOfPlayPileAction(Player performingPlayer, SelectCardsAction selectCardAction) {
        super(performingPlayer, ActionType.PLACE_CARD, Progress.values());
        _selectCardAction = selectCardAction;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!getProgress(Progress.cardSelected)) {
            if (_selectCardAction.wasCarriedOut()) {
                _cardBeingPlaced = _selectCardAction.getSelectedCards().stream().toList().getFirst();
                setProgress(Progress.cardSelected);
            } else {
                return _selectCardAction;
            }
        }

        if (!getProgress(Progress.wasCarriedOut)) {
            cardGame.getGameState().removeCardsFromZone(_performingPlayerId, List.of(_cardBeingPlaced));
            cardGame.sendMessage(_performingPlayerId + " puts " +
                    _cardBeingPlaced.getCardLink() + " from hand on bottom of their play pile");
            cardGame.getGameState().addCardToZone(_cardBeingPlaced, Zone.PLAY_PILE, false);
            setProgress(Progress.wasCarriedOut);
        }

        return getNextAction();
    }
}