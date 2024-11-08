package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class PlayOutOptionalBeforeResponsesEffect extends UnrespondableEffect {
    private final SystemQueueAction _action;
    private final Set<PhysicalCard> _cardTriggersUsed;
    private final ActionOrder _actionOrder;
    private final int _passCount;
    private final Effect _effect;
    private final ActionsEnvironment _actionsEnvironment;

    PlayOutOptionalBeforeResponsesEffect(SystemQueueAction action, Set<PhysicalCard> cardTriggersUsed,
                                         ActionOrder actionOrder, int passCount, Effect effect) {
        super(action.getGame());
        _action = action;
        _cardTriggersUsed = cardTriggersUsed;
        _actionOrder = actionOrder;
        _passCount = passCount;
        _effect = effect;
        _actionsEnvironment = _game.getActionsEnvironment();
    }

    @Override
    public void doPlayEffect() {
        final String activePlayer = _actionOrder.getNextPlayer();

        final List<Action> optionalBeforeTriggers =
                _actionsEnvironment.getOptionalBeforeTriggers(activePlayer, _effect);
        // Remove triggers already resolved
        optionalBeforeTriggers.removeIf(action -> _cardTriggersUsed.contains(action.getActionSource()));

        final List<Action> optionalBeforeActions =
                _actionsEnvironment.getOptionalBeforeActions(activePlayer, _effect);

        List<Action> possibleActions = new LinkedList<>(optionalBeforeTriggers);
        possibleActions.addAll(optionalBeforeActions);

        if (possibleActions.isEmpty()) {
            if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                Effect effect = new PlayOutOptionalBeforeResponsesEffect(_action, _cardTriggersUsed, _actionOrder,
                        _passCount + 1, _effect);
                _action.insertEffect(effect);
            }
        } else {
            Player decidingPlayer = _game.getGameState().getPlayer(activePlayer);
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(decidingPlayer,
                            _effect.getText() + " - Optional \"is about to\" responses", possibleActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _actionsEnvironment.addActionToStack(action);
                                if (optionalBeforeTriggers.contains(action))
                                    _cardTriggersUsed.add(action.getActionSource());
                                _action.insertEffect(new PlayOutOptionalBeforeResponsesEffect(
                                        _action, _cardTriggersUsed, _actionOrder, 0, _effect));
                            } else {
                                if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                                    _action.insertEffect(new PlayOutOptionalBeforeResponsesEffect(
                                            _action, _cardTriggersUsed, _actionOrder, _passCount + 1, _effect));
                                }
                            }
                        }
                    });
        }
    }
}