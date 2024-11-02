package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.ActionOrder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class PlayOutOptionalAfterResponsesEffect extends UnrespondableEffect {
    private final SystemQueueAction _action;
    private final ActionOrder _actionOrder;
    private final int _passCount;
    private final Collection<? extends EffectResult> _effectResults;
    private final ActionsEnvironment _actionsEnvironment;

    PlayOutOptionalAfterResponsesEffect(SystemQueueAction action, ActionOrder actionOrder, int passCount,
                                        Collection<? extends EffectResult> effectResults) {
        super(action.getGame());
        _action = action;
        _actionOrder = actionOrder;
        _passCount = passCount;
        _effectResults = effectResults;
        _actionsEnvironment = _game.getActionsEnvironment();
    }

    @Override
    public void doPlayEffect() {
        final String activePlayer = _actionOrder.getNextPlayer();

        final Map<Action, EffectResult> optionalAfterTriggers =
                _actionsEnvironment.getOptionalAfterTriggers(activePlayer, _effectResults);

        List<Action> possibleActions = new LinkedList<>(optionalAfterTriggers.keySet());
        possibleActions.addAll(_actionsEnvironment.getOptionalAfterActions(activePlayer, _effectResults));

        if (possibleActions.isEmpty()) {
            if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                _action.insertEffect(new PlayOutOptionalAfterResponsesEffect(
                        _action, _actionOrder, _passCount + 1, _effectResults));
            }
        } else {
            _game.getUserFeedback().sendAwaitingDecision(activePlayer,
                    new CardActionSelectionDecision(1, "Optional responses", possibleActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            final int nextPassCount;
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _actionsEnvironment.addActionToStack(action);
                                if (optionalAfterTriggers.containsKey(action))
                                    optionalAfterTriggers.get(action).optionalTriggerUsed(action);
                                nextPassCount = 0;
                            } else {
                                nextPassCount = _passCount + 1;
                            }
                            if (nextPassCount < _actionOrder.getPlayerCount())
                                _action.insertEffect(new PlayOutOptionalAfterResponsesEffect(
                                        _action, _actionOrder, nextPassCount, _effectResults));
                        }
                    });
        }
    }
}