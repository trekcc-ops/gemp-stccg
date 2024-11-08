package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.game.Player;

import java.util.List;

final class PlayOutAllActionsIfEffectNotCancelledEffect extends UnrespondableEffect {
    private final SystemQueueAction _action;
    private final List<Action> _actions;
    private final List<Action> _actionStack;

    PlayOutAllActionsIfEffectNotCancelledEffect(SystemQueueAction action, List<Action> actions) {
        super(action.getGame());
        _action = action;
        _actions = actions;
        _actionStack = _game.getActionsEnvironment().getActionStack();
    }

    @Override
    public void doPlayEffect() {
        if (_actions.size() == 1) {
            _actionStack.add(_actions.getFirst());
        } else if (areAllActionsTheSame(_actions)) {
            Action anyAction = _actions.getFirst();
            _actions.remove(anyAction);
            _actionStack.add(anyAction);
            _action.insertEffect(new PlayOutAllActionsIfEffectNotCancelledEffect(_action, _actions));
        } else {
            Player player = _game.getCurrentPlayer();
            _game.getUserFeedback().sendAwaitingDecision(
                    new ActionSelectionDecision(player, "Required responses", _actions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            _actionStack.add(action);
                            _actions.remove(action);
                            _action.insertEffect(new PlayOutAllActionsIfEffectNotCancelledEffect(_action, _actions));
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