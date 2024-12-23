package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

final class PlayOutAllActionsIfEffectNotCancelledEffect extends UnrespondableEffect {
    private final SystemQueueAction _action;
    private final List<Action> _actions;

    PlayOutAllActionsIfEffectNotCancelledEffect(DefaultGame game, SystemQueueAction action, List<Action> actions) {
        super(game);
        _action = action;
        _actions = actions;
    }


    @Override
    public void doPlayEffect() {
        ActionsEnvironment environment = _game.getActionsEnvironment();
        if (_actions.size() == 1) {
            environment.addActionToStack(_actions.getFirst());
        } else if (areAllActionsTheSame(_actions)) {
            Action anyAction = _actions.getFirst();
            _actions.remove(anyAction);
            environment.addActionToStack(anyAction);
            _action.insertEffect(new PlayOutAllActionsIfEffectNotCancelledEffect(_game, _action, _actions));
        } else {
            _game.getUserFeedback().sendAwaitingDecision(
                    new ActionSelectionDecision(_game.getCurrentPlayer(), "Required responses", _actions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            environment.addActionToStack(action);
                            _actions.remove(action);
                            _action.insertEffect(
                                    new PlayOutAllActionsIfEffectNotCancelledEffect(_game, _action, _actions));
                        }
                    });
        }
    }

    private static boolean areAllActionsTheSame(List<? extends Action> actions) {
        boolean result = true;
        Action firstAction = actions.getFirst();
        if (firstAction.getActionSource() == null)
                result = false;
        for (Action action : actions) {
            if (action.getActionSource() == null)
                result = false;
            else if (action.getActionSource().getBlueprint() != firstAction.getActionSource().getBlueprint())
                result = false;
        }
        return result;
    }
}