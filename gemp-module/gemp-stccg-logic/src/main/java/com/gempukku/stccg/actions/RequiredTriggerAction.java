package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class RequiredTriggerAction extends AbstractCostToEffectAction {
    private final PhysicalCard _physicalCard;

    private boolean _sentMessage;
    private String _message;

    public RequiredTriggerAction(PhysicalCard physicalCard) {
        super(physicalCard.getOwner(), ActionType.TRIGGER);
        _physicalCard = physicalCard;
        setText("Required trigger from " + _physicalCard.getCardLink());
        _message = _physicalCard.getCardLink() + " required triggered effect is used";
    }

    @Override
    public PhysicalCard getActionSource() {
        return _physicalCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _physicalCard;
    }

    public void setMessage(String message) {
        _message = message;
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

        if (isCostFailed()) {
            return null;
        } else {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            return getNextEffect();
        }
    }
}