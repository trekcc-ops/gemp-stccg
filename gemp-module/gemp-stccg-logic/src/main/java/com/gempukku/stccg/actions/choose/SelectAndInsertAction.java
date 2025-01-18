package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class SelectAndInsertAction extends ActionyAction {
    private final List<TopLevelSelectableAction> _actionsToChooseFrom = new LinkedList<>();
    private final ActionyAction _parentAction;
    private Action _chosenAction;
    private enum Progress { actionSelected }

    public SelectAndInsertAction(ActionyAction parentAction, Player selectingPlayer,
                                 TopLevelSelectableAction... actions) {
        super(selectingPlayer, "Choose an action", ActionType.SELECT_ACTION, Progress.values());
        _actionsToChooseFrom.addAll(Arrays.asList(actions));
        _parentAction = parentAction;
    }

    public boolean requirementsAreMet(DefaultGame game) {
        boolean result = false;
        for (Action action : _actionsToChooseFrom) {
            if (action.canBeInitiated(game)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!getProgress(Progress.actionSelected)) {
            List<Action> performableActions = new LinkedList<>();
            List<String> actionTexts = new LinkedList<>();
            for (Action action : _actionsToChooseFrom) {
                if (action.canBeInitiated(cardGame)) {
                    performableActions.add(action);
                    actionTexts.add(action.getActionSelectionText(cardGame));
                }
            }
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            AwaitingDecision decision = new MultipleChoiceAwaitingDecision(performingPlayer, "Choose an action",
                    actionTexts) {
                @Override
                protected void validDecisionMade(int index, String result) {
                    try {
                        _chosenAction = performableActions.get(index);
                        _parentAction.insertAction(_chosenAction);
                        setProgress(Progress.actionSelected);
                        _wasCarriedOut = true;
                    } catch(NoSuchElementException exp) {
                        cardGame.sendErrorMessage(exp);
                    }
                }
            };
            cardGame.getUserFeedback().sendAwaitingDecision(decision);
        }
        return getNextAction();
    }

    @Override
    public boolean wasCarriedOut() {
        return _wasCarriedOut;
    }

}