package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ActivateCardAction extends ActionyAction {

    private final PhysicalCard _physicalCard;
    private ActivateCardEffect _activateCardEffect;
    private boolean _sentMessage;
    private boolean _activated;
    private boolean _prevented;

    public ActivateCardAction(PhysicalCard physicalCard) {
        super(physicalCard.getOwner(), ActionType.SPECIAL_ABILITY);
        _physicalCard = physicalCard;
        setText("Use " + _physicalCard.getFullName());
    }

    @Override
    public PhysicalCard getActionSource() {
        return _physicalCard;
    }

    // TODO - Not sure this is accurate. Also not sure we need this class at all.
    public boolean requirementsAreMet(DefaultGame game) { return true; }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _physicalCard;
    }

    public void prevent() {
        _prevented = true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (!_sentMessage) {
            _sentMessage = true;
            if (_physicalCard != null && _physicalCard.getZone().isInPlay()) {
                DefaultGame game = _physicalCard.getGame();
                game.getGameState().activatedCard(getPerformingPlayerId(), _physicalCard);
                game.sendMessage(_physicalCard.getCardLink() + " is used");
            }
        }

        if (!isCostFailed()) {
            Action cost = getNextCost();
            if (cost != null)
                return cost;

            if (!_activated) {
                _activated = true;
                return new SubAction(this, new ActivateCardEffect(_physicalCard));
            }

            if (_activateCardEffect.getActivateCardResult().isEffectCancelled())
                return null;
            if (!_prevented)
                return getNextAction();
        }
        return null;
    }

}