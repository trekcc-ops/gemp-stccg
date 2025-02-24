package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class OptionalTriggerAction extends ActionyAction implements TopLevelSelectableAction {
    private final PhysicalCard _performingCard;
    private enum Progress {}
    private ActionBlueprint _actionBlueprint;

    public OptionalTriggerAction(PhysicalCard physicalCard) {
        super(physicalCard.getGame(), physicalCard.getOwner(), "Optional trigger from " + physicalCard.getCardLink(),
                ActionType.USE_GAME_TEXT,
                Progress.values());
        _performingCard = physicalCard;
    }

    public OptionalTriggerAction(PhysicalCard physicalCard, ActionBlueprint actionBlueprint) {
        this(physicalCard);
        _actionBlueprint = actionBlueprint;
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
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        if (!isCostFailed()) {
            Action cost = getNextCost();
            if (cost != null)
                return cost;

            if (_actionBlueprint != null) {
                cardGame.getGameState().getModifiersQuerying().getUntilEndOfTurnLimitCounter(_actionBlueprint).countUse();
            }
            Action action = getNextAction();
            if (action == null)
                setAsSuccessful();
            return action;
        } else {
            setAsFailed();
        }
        return null;
    }

}