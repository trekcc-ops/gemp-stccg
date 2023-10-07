package com.gempukku.lotro.actions;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.game.ST1EGame;

public abstract class AbstractPlayCardAction extends AbstractCostToEffectAction<ST1EGame> {

    protected PhysicalCard _actionSource;
    protected PhysicalCard _cardToPlay;
    protected Zone _playedFromZone;
    protected boolean _reshuffle;
    protected boolean _placeOutOfPlay;
    protected String _text;
    private boolean _virtualCardAction;
    private String _performingPlayer;
    /**
     * Creates an action for playing the specified card.
     * @param cardToPlay the card to play
     * @param actionSource the card to initiate the deployment
     */
    public AbstractPlayCardAction(PhysicalCard cardToPlay, PhysicalCard actionSource) {
        _cardToPlay = cardToPlay;
        _playedFromZone = cardToPlay.getZone();
        _actionSource = actionSource;
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
        return  _cardToPlay;
    }

    public PhysicalCard getPlayedCard() {
        return _cardToPlay;
    }

    /**
     * Gets the zone the card is being played or deployed from.
     * @return the zone
     */
    public Zone getPlayingFromZone() {
        return _playedFromZone;
    }

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

    public abstract Effect<ST1EGame> nextEffect(ST1EGame game);
    public void setVirtualCardAction(boolean virtualCardAction) { _virtualCardAction = virtualCardAction; }
    public boolean isVirtualCardAction() { return _virtualCardAction; }

    public void setPerformingPlayer(String playerId) {
        _performingPlayer = playerId;
    }

    public String getPerformingPlayer() {
        return _performingPlayer;
    }

}
