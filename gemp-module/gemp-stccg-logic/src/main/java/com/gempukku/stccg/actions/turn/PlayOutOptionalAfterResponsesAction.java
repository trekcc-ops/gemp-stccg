package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlayOutOptionalAfterResponsesAction extends SystemQueueAction {
    private final SystemQueueAction _action;
    private final ActionOrder _actionOrder;
    private final int _passCount;
    private final Collection<EffectResult> _effectResults;
    private final ActionsEnvironment _actionsEnvironment;

    public PlayOutOptionalAfterResponsesAction(DefaultGame game, SystemQueueAction action, ActionOrder actionOrder,
                                        int passCount, Collection<EffectResult> effectResults) {
        super(game);
        _action = action;
        _actionOrder = actionOrder;
        _passCount = passCount;
        _effectResults = effectResults;
        _actionsEnvironment = game.getActionsEnvironment();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        final String activePlayer = _actionOrder.getNextPlayer();

        final Map<Action, EffectResult> optionalAfterTriggers =
                _actionsEnvironment.getOptionalAfterTriggers(activePlayer, _effectResults);

        List<Action> possibleActions = new LinkedList<>(optionalAfterTriggers.keySet());
        possibleActions.addAll(_actionsEnvironment.getOptionalAfterActions(activePlayer, _effectResults));

        if (possibleActions.isEmpty()) {
            if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                _action.insertAction(new PlayOutOptionalAfterResponsesAction(cardGame,
                        _action, _actionOrder, _passCount + 1, _effectResults));
            }
        } else {
            Player decidingPlayer = cardGame.getGameState().getPlayer(activePlayer);
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(decidingPlayer, "Optional responses", possibleActions) {
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
                                _action.insertAction(new PlayOutOptionalAfterResponsesAction(cardGame,
                                        _action, _actionOrder, nextPassCount, _effectResults));
                        }
                    });
        }
        return getNextAction();
    }

    public Collection<EffectResult> getEffectResults() { return _effectResults; }

}