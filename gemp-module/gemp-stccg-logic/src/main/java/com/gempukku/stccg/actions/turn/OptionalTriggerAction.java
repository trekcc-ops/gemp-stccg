package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.blueprints.actionsource.ActionSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class OptionalTriggerAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalCard _performingCard;
    private enum Progress { sentMessage }
    private ActionSource _actionSource;

    public OptionalTriggerAction(PhysicalCard physicalCard) {
        super(physicalCard.getOwner(), "Optional trigger from " + physicalCard.getCardLink(), ActionType.OTHER,
                Progress.values());
        _performingCard = physicalCard;
    }

    public OptionalTriggerAction(PhysicalCard physicalCard, ActionSource actionSource) {
        this(physicalCard);
        _actionSource = actionSource;
    }

    public boolean requirementsAreMet(DefaultGame cardGame) { return true; }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public int getCardIdForActionSelection() {
        return _performingCard.getCardId();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (!getProgress(Progress.sentMessage)) {
            setProgress(Progress.sentMessage);
            if (_performingCard != null) {
                cardGame.getGameState().activatedCard(getPerformingPlayerId(), _performingCard);
                cardGame.sendMessage(_performingCard.getCardLink() + " optional triggered effect is used");
            }
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