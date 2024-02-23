package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.Effect;

public class OptionalTriggerAction extends AbstractCostToEffectAction {
    private PhysicalCard _physicalCard;
    private final PhysicalCard _actionAttachedToCard;

    private String _message;

    private boolean _sentMessage;
    private String _triggerIdentifier;
    private final DefaultGame _game;

    public OptionalTriggerAction(String triggerIdentifier, PhysicalCard attachedToCard) {
        _game = attachedToCard.getGame();
        _actionAttachedToCard = attachedToCard;
        _triggerIdentifier = triggerIdentifier;
    }

    public OptionalTriggerAction(PhysicalCard physicalCard) {
        _game = physicalCard.getGame();
        _physicalCard = physicalCard;
        _actionAttachedToCard = physicalCard;

        setText("Optional trigger from " + _physicalCard.getCardLink());
        _message = _physicalCard.getCardLink() + " optional triggered effect is used";
        _triggerIdentifier = String.valueOf(physicalCard.getCardId());
    }

    public void setTriggerIdentifier(String triggerIdentifier) {
        _triggerIdentifier = triggerIdentifier;
    }

    public String getTriggerIdentifier() {
        return _triggerIdentifier;
    }

    public void setMessage(String message) {
        _message = message;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.TRIGGER;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _physicalCard;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _actionAttachedToCard;
    }

    @Override
    public Effect nextEffect() {
        if (!_sentMessage) {
            _sentMessage = true;
            if (_physicalCard != null)
                _game.getGameState().activatedCard(getPerformingPlayer(), _physicalCard);
            if (_message != null)
                _game.getGameState().sendMessage(_message);
        }

        if (!isCostFailed()) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            return getNextEffect();
        }
        return null;
    }

    @Override
    public DefaultGame getGame() { return _game; }
}
