package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.blueprints.actionsource.ActionSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class OptionalTriggerAction extends ActionyAction {
    private final PhysicalCard _physicalCard;
    private final PhysicalCard _actionAttachedToCard;

    private String _message;

    private boolean _sentMessage;
    private ActionSource _actionSource;

    public OptionalTriggerAction(PhysicalCard physicalCard) {
        super(physicalCard.getOwner(), "Optional trigger from " + physicalCard.getCardLink(), ActionType.OTHER);
        _physicalCard = physicalCard;
        _actionAttachedToCard = physicalCard;
        _message = _physicalCard.getCardLink() + " optional triggered effect is used";
    }

    public OptionalTriggerAction(PhysicalCard physicalCard, ActionSource actionSource) {
        this(physicalCard);
        _actionSource = actionSource;
    }

    public void setMessage(String message) {
        _message = message;
    }
    public boolean requirementsAreMet(DefaultGame cardGame) { return true; }

    @Override
    public PhysicalCard getActionSource() {
        return _physicalCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _actionAttachedToCard;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (!_sentMessage) {
            _sentMessage = true;
            if (_physicalCard != null)
                cardGame.getGameState().activatedCard(getPerformingPlayerId(), _physicalCard);
            if (_message != null)
                cardGame.sendMessage(_message);
        }

        if (!isCostFailed()) {
            Action cost = getNextCost();
            if (cost != null)
                return cost;

            if (_actionSource != null) {
                cardGame.getModifiersQuerying().getUntilEndOfTurnLimitCounter(_actionSource).countUse();
            }
            return getNextAction();
        }
        return null;
    }

}