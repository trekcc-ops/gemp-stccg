package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.Effect;

public abstract class AbstractPlayCardAction extends AbstractCostToEffectAction {

    protected final PhysicalCard _actionSource;
    protected boolean _reshuffle;
    protected boolean _placeOutOfPlay;
    protected String _text;
    private boolean _virtualCardAction;
    private String _performingPlayer;
    protected final Action _thisAction;
    /**
     * Creates an action for playing the specified card.
     * @param actionSource the card to initiate the deployment
     */
    public AbstractPlayCardAction(PhysicalCard actionSource) {
        _actionSource = actionSource;
        _thisAction = this;
    }

    @Override
    public boolean canBeInitiated() {
        return _actionSource.canBePlayed();
    }

    public ActionType getType() {
        return ActionType.PLAY_CARD;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _actionSource;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return getPlayedCard();
    }

    public abstract PhysicalCard getPlayedCard();

    public String getText() {
        return _text;
    }

    /**
     * Sets the text shown for the action selection on the User Interface.
     * @param text the text to show for the action selection
     */
    public void setText(String text) {
        _text = text;
    }

    /**
     * Sets if the card pile the card is played from is reshuffled.
     * @param reshuffle true if pile the card is played from is reshuffled, otherwise false
     */
    public void setReshuffle(boolean reshuffle) {
        _reshuffle = reshuffle;
    }

    /**
     * Sets that the card is to be placed out of play when played.
     * @param placeOutOfPlay true if card is to be placed out of play
     */
    public void setPlaceOutOfPlay(boolean placeOutOfPlay) {
        _placeOutOfPlay = placeOutOfPlay;
    }

    /**
     * Determines if the card is to be placed out of play when played.
     * @return true or false
     */
    public boolean isToBePlacedOutOfPlay() {
        return _placeOutOfPlay;
    }

    public abstract Effect nextEffect();
    public void setVirtualCardAction(boolean virtualCardAction) { _virtualCardAction = virtualCardAction; }
    public boolean isVirtualCardAction() { return _virtualCardAction; }

    public void setPerformingPlayer(String playerId) {
        _performingPlayer = playerId;
    }

    public String getPerformingPlayer() {
        return _performingPlayer;
    }

}
