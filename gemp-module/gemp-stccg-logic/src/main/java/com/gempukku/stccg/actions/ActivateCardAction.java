package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class ActivateCardAction extends AbstractCostToEffectAction {

    protected final PhysicalCard _physicalCard;
    protected ActivateCardEffect _activateCardEffect;
    protected boolean _sentMessage;
    protected boolean _activated;
    protected boolean _prevented;
    protected final DefaultGame _game;

    public ActivateCardAction(PhysicalCard physicalCard) {
        super(physicalCard.getOwner(), ActionType.SPECIAL_ABILITY);
        _game = physicalCard.getGame();
        _physicalCard = physicalCard;
        setText("Use " + _physicalCard.getFullName());
    }

    @Override
    public PhysicalCard getActionSource() {
        return _physicalCard;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _physicalCard;
    }

    public void prevent() {
        _prevented = true;
    }

    @Override
    public Effect nextEffect() throws InvalidGameLogicException {
        if (!_sentMessage) {
            _sentMessage = true;
            if (_physicalCard != null && _physicalCard.getZone().isInPlay()) {
                _game.getGameState().activatedCard(getPerformingPlayerId(), _physicalCard);
                _game.sendMessage(_physicalCard.getCardLink() + " is used");
            }
        }

        if (!isCostFailed()) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            if (!_activated) {
                _activated = true;
                _activateCardEffect = new ActivateCardEffect(_physicalCard);
                return _activateCardEffect;
            }

            if (_activateCardEffect.getActivateCardResult().isEffectCancelled())
                return null;
            if (!_prevented)
                return getNextEffect();
        }
        return null;
    }

    @Override
    public DefaultGame getGame() { return _game; }
}
