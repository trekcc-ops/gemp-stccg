package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.ActionsEnvironment;

import java.util.List;

public final class PlayOutAllSubActionsIfActionNotCancelledAction extends SystemQueueAction {
    private final SystemQueueAction _action;
    private final List<Action> _actions;
    private boolean _initialized;

    public PlayOutAllSubActionsIfActionNotCancelledAction(DefaultGame game, SystemQueueAction action,
                                                          List<Action> actions) {
        super(game);
        _action = action;
        _actions = actions;
    }


    public void doPlayEffect(DefaultGame cardGame) {
        ActionsEnvironment environment = cardGame.getActionsEnvironment();
        if (_actions.size() == 1) {
            environment.addActionToStack(_actions.getFirst());
        } else if (areAllActionsTheSame(_actions)) {
            Action anyAction = _actions.getFirst();
            _actions.remove(anyAction);
            environment.addActionToStack(anyAction);
            _action.insertEffect(cardGame,
                    new PlayOutAllSubActionsIfActionNotCancelledAction(cardGame, _action, _actions));
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new ActionSelectionDecision(cardGame.getCurrentPlayer(), "Required responses", _actions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            environment.addActionToStack(action);
                            _actions.remove(action);
                            _action.insertEffect(cardGame,
                                    new PlayOutAllSubActionsIfActionNotCancelledAction(cardGame, _action, _actions));
                        }
                    });
        }
    }

    private static boolean areAllActionsTheSame(List<? extends Action> actions) {
        boolean result = true;
        Action firstAction = actions.getFirst();
        if (firstAction.getPerformingCard() == null)
                result = false;
        for (Action action : actions) {
            if (action.getPerformingCard() == null)
                result = false;
            else if (action.getPerformingCard().getBlueprint() != firstAction.getPerformingCard().getBlueprint())
                result = false;
        }
        return result;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (!_initialized) {
            _initialized = true;
            doPlayEffect(cardGame);
        }
        return getNextAction();
    }
}