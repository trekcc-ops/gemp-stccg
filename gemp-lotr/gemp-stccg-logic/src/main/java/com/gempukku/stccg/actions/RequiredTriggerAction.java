package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.effects.Effect;

public class RequiredTriggerAction extends AbstractCostToEffectAction {
    private final PhysicalCard _physicalCard;

    private boolean _sentMessage;
    private String _message;

    public RequiredTriggerAction(PhysicalCard physicalCard) {
        _physicalCard = physicalCard;
        if (physicalCard != null) {
            setText("Required trigger from " + GameUtils.getCardLink(_physicalCard));
            _message = GameUtils.getCardLink(_physicalCard) + " required triggered effect is used";
        }
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
        return _physicalCard;
    }

    public void setMessage(String message) {
        _message = message;
    }

    @Override
    public Effect nextEffect(DefaultGame game) {
        if (!_sentMessage) {
            _sentMessage = true;
            if (_physicalCard != null)
                game.getGameState().activatedCard(getPerformingPlayer(), _physicalCard);
            if (_message != null)
                game.getGameState().sendMessage(_message);
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
