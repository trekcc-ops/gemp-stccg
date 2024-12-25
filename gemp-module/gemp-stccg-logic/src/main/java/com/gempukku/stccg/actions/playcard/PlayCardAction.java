package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collections;

public abstract class PlayCardAction extends ActionyAction {

    final PhysicalCard _actionSource;
    private boolean _cardWasRemoved, _cardHasEnteredPlay;
    private boolean _virtualCardAction;
    final PhysicalCard _cardEnteringPlay;
    protected final Zone _fromZone;
    final Zone _toZone;
    private Effect _finalEffect;

    /**
     * Creates an action for playing the specified card.
     * @param actionSource the card to initiate the deployment
     */
    public PlayCardAction(PhysicalCard actionSource, PhysicalCard cardEnteringPlay, String performingPlayerId,
                          Zone toZone, ActionType actionType) {
        super(cardEnteringPlay.getGame().getPlayer(performingPlayerId), actionType);
        _actionSource = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _fromZone = cardEnteringPlay.getZone();
        _toZone = toZone;
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return _cardEnteringPlay.canBePlayed(cardGame);
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _actionSource;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return getCardEnteringPlay();
    }

    public PhysicalCard getCardEnteringPlay() { return _cardEnteringPlay; }

    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_cardWasRemoved) {
            _cardWasRemoved = true;
            if (_fromZone == Zone.DRAW_DECK) {
                cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " shuffles their deck");
                cardGame.getGameState().shuffleDeck(_cardEnteringPlay.getOwnerName());
            }
        }

        if (!_cardHasEnteredPlay) {
            _cardHasEnteredPlay = true;
            putCardIntoPlay(cardGame);
        }

        return getNextAction();
    }
    
    protected void putCardIntoPlay(DefaultGame game) {
        GameState gameState = game.getGameState();
        gameState.removeCardsFromZone(_cardEnteringPlay.getOwnerName(), Collections.singleton(_cardEnteringPlay));
        gameState.addCardToZone(_cardEnteringPlay, _toZone);
        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(this, _fromZone, _cardEnteringPlay));
        game.sendMessage(_cardEnteringPlay.getOwnerName() + " played " +
                _cardEnteringPlay.getCardLink() +  " from " + _fromZone.getHumanReadable() +
                " to " + _toZone.getHumanReadable());
    }

    public void setVirtualCardAction(boolean virtualCardAction) { _virtualCardAction = virtualCardAction; }
    public boolean isVirtualCardAction() { return _virtualCardAction; }

    public boolean wasCarriedOut() {
        return _cardHasEnteredPlay;
    }

}