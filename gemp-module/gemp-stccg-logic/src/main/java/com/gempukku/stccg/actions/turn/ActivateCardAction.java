package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class ActivateCardAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _physicalCard;
    private boolean _sentMessage;
    private boolean _activated;
    private boolean _prevented;

    public ActivateCardAction(PhysicalCard physicalCard) {
        super(physicalCard.getOwner(), "Use " + physicalCard.getFullName(), ActionType.SPECIAL_ABILITY);
        _physicalCard = physicalCard;
    }

    @Override
    public PhysicalCard getPerformingCard() {
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
                cardGame.getActionsEnvironment().emitEffectResult(
                        new ActionResult(ActionResult.Type.ACTIVATE, this, _physicalCard));
            }

            if (!_prevented)
                return getNextAction();
        }
        return null;
    }

}