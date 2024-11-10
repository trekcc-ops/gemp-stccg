package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public abstract class PlayCardAction extends AbstractCostToEffectAction {

    protected final PhysicalCard _actionSource;
    private boolean _actionWasInitiated, _cardWasRemoved, _cardHasEnteredPlay;
    protected String _text;
    private boolean _virtualCardAction;
    protected final PhysicalCard _cardEnteringPlay;
    protected final Zone _fromZone;
    protected final Zone _toZone;
    protected Effect _finalEffect;

    /**
     * Creates an action for playing the specified card.
     * @param actionSource the card to initiate the deployment
     */
    public PlayCardAction(PhysicalCard actionSource, PhysicalCard cardEnteringPlay, String performingPlayerId,
                          Zone toZone, ActionType actionType) {
        super(performingPlayerId, actionType);
        _actionSource = actionSource;
        _cardEnteringPlay = cardEnteringPlay;
        _fromZone = cardEnteringPlay.getZone();
        _toZone = toZone;
    }

    @Override
    public boolean canBeInitiated(DefaultGame cardGame) {
        if (!_cardEnteringPlay.canBePlayed(cardGame))
            return false;
        return costsCanBePaid();
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

    public String getText(DefaultGame game) {
        return _text;
    }

    /**
     * Sets the text shown for the action selection on the User Interface.
     * @param text the text to show for the action selection
     */
    public void setText(String text) {
        _text = text;
    }

    protected Effect getFinalEffect() { return new PlayCardEffect(_performingPlayerId, _fromZone, _cardEnteringPlay, _toZone); }

    public Effect nextEffect(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_actionWasInitiated) {
            _actionWasInitiated = true;
            // TODO - Star Wars code used beginPlayCard method here
        }

        Effect cost = getNextCost();
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
            _finalEffect = getFinalEffect();
            return _finalEffect;
        }

        return getNextEffect();
    }

    public void setVirtualCardAction(boolean virtualCardAction) { _virtualCardAction = virtualCardAction; }
    public boolean isVirtualCardAction() { return _virtualCardAction; }

    public boolean wasCarriedOut() {
        return _cardHasEnteredPlay && _finalEffect != null && _finalEffect.wasCarriedOut();
    }

}