package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public final class PlayOutAllActionsIfEffectNotCancelledEffect extends DefaultEffect {
    private final SystemQueueAction _action;
    private final List<Action> _actions;

    public PlayOutAllActionsIfEffectNotCancelledEffect(DefaultGame game, SystemQueueAction action, List<Action> actions) {
        super(game, "none");
        _action = action;
        _actions = actions;
    }


    public void doPlayEffect() {
        ActionsEnvironment environment = _game.getActionsEnvironment();
        if (_actions.size() == 1) {
            environment.addActionToStack(_actions.getFirst());
        } else if (areAllActionsTheSame(_actions)) {
            Action anyAction = _actions.getFirst();
            _actions.remove(anyAction);
            environment.addActionToStack(anyAction);
            _action.insertEffect(_game,
                    new SubAction(_action, new PlayOutAllActionsIfEffectNotCancelledEffect(_game, _action, _actions)));
        } else {
            _game.getUserFeedback().sendAwaitingDecision(
                    new ActionSelectionDecision(_game.getCurrentPlayer(), "Required responses", _actions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            environment.addActionToStack(action);
                            _actions.remove(action);
                            _action.insertEffect(_game, new SubAction(_action,
                                    new PlayOutAllActionsIfEffectNotCancelledEffect(_game, _action, _actions)));
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
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    public FullEffectResult playEffectReturningResult() {
        doPlayEffect();
        return new FullEffectResult(true);
    }

}