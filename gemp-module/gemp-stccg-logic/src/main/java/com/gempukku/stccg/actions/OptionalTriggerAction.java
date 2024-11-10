package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.blueprints.actionsource.ActionSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class OptionalTriggerAction extends AbstractCostToEffectAction {
    private final PhysicalCard _physicalCard;
    private final PhysicalCard _actionAttachedToCard;

    private String _message;

    private boolean _sentMessage;
    private ActionSource _actionSource;

    public OptionalTriggerAction(PhysicalCard physicalCard) {
        super(physicalCard.getOwner(), ActionType.TRIGGER);
        _physicalCard = physicalCard;
        _actionAttachedToCard = physicalCard;

        setText("Optional trigger from " + _physicalCard.getCardLink());
        _message = _physicalCard.getCardLink() + " optional triggered effect is used";
    }

    public OptionalTriggerAction(PhysicalCard physicalCard, ActionSource actionSource) {
        this(physicalCard);
        _actionSource = actionSource;
    }

    public void setMessage(String message) {
        _message = message;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _physicalCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _actionAttachedToCard;
    }

    @Override
    public Effect nextEffect(DefaultGame cardGame) {
        if (!_sentMessage) {
            _sentMessage = true;
            if (_physicalCard != null)
                cardGame.getGameState().activatedCard(getPerformingPlayerId(), _physicalCard);
            if (_message != null)
                cardGame.sendMessage(_message);
        }

        if (!isCostFailed()) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            if (_actionSource != null) {
                cardGame.getModifiersQuerying().getUntilEndOfTurnLimitCounter(_actionSource).countUse();
            }
            return getNextEffect();
        }
        return null;
    }

}