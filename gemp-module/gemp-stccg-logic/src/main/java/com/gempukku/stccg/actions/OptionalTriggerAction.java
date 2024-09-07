package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.sources.ActionSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class OptionalTriggerAction extends AbstractCostToEffectAction {
    private PhysicalCard _physicalCard;
    private final PhysicalCard _actionAttachedToCard;

    private String _message;

    private boolean _sentMessage;
    private String _triggerIdentifier;
    private final DefaultGame _game;
    private ActionSource _actionSource;

    public OptionalTriggerAction(String triggerIdentifier, PhysicalCard attachedToCard) {
        _game = attachedToCard.getGame();
        _actionAttachedToCard = attachedToCard;
        _triggerIdentifier = triggerIdentifier;
    }

    public OptionalTriggerAction(PhysicalCard physicalCard) {
        super(physicalCard.getOwner(), ActionType.TRIGGER);
        _game = physicalCard.getGame();
        _physicalCard = physicalCard;
        _actionAttachedToCard = physicalCard;

        setText("Optional trigger from " + _physicalCard.getCardLink());
        _message = _physicalCard.getCardLink() + " optional triggered effect is used";
        _triggerIdentifier = String.valueOf(physicalCard.getCardId());
    }

    public OptionalTriggerAction(PhysicalCard physicalCard, ActionSource actionSource) {
        this(physicalCard);
        _actionSource = actionSource;
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
                _game.getGameState().activatedCard(getPerformingPlayerId(), _physicalCard);
            if (_message != null)
                _game.sendMessage(_message);
        }

        if (!isCostFailed()) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            if (_actionSource != null) {
                _game.getModifiersQuerying().getUntilEndOfTurnLimitCounter(_actionSource).countUse();
            }
            return getNextEffect();
        }
        return null;
    }

    @Override
    public DefaultGame getGame() { return _game; }
}
