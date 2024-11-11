package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

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
    public PhysicalCard getActionSource() {
        return _actionSource;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return getCardEnteringPlay();
    }

    public PhysicalCard getCardEnteringPlay() { return _cardEnteringPlay; }

    protected Effect getFinalEffect() {
        return new PlayCardEffect(_performingPlayerId, _fromZone, _cardEnteringPlay, _toZone);
    }

    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_cardWasRemoved) {
            _cardWasRemoved = true;
            cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " plays " +
                    _cardEnteringPlay.getCardLink() +  " from " + _fromZone.getHumanReadable() +
                    " to " + _toZone.getHumanReadable());
            if (_fromZone == Zone.DRAW_DECK) {
                cardGame.sendMessage(_cardEnteringPlay.getOwnerName() + " shuffles their deck");
                cardGame.getGameState().shuffleDeck(_cardEnteringPlay.getOwnerName());
            }
        }

        if (!_cardHasEnteredPlay) {
            _cardHasEnteredPlay = true;
            return new SubAction(this, getFinalEffect());
        }

        return getNextAction();
    }

    public void setVirtualCardAction(boolean virtualCardAction) { _virtualCardAction = virtualCardAction; }
    public boolean isVirtualCardAction() { return _virtualCardAction; }

    public boolean wasCarriedOut() {
        return _cardHasEnteredPlay && _finalEffect != null && _finalEffect.wasCarriedOut();
    }

}