package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PlayOutOptionalBeforeResponsesEffect extends DefaultEffect {
    private final SystemQueueAction _action;
    private final Set<PhysicalCard> _cardTriggersUsed;
    private final ActionOrder _actionOrder;
    private final int _passCount;
    private final Effect _effect;
    private final ActionsEnvironment _actionsEnvironment;

    public PlayOutOptionalBeforeResponsesEffect(SystemQueueAction action, Set<PhysicalCard> cardTriggersUsed,
                                         ActionOrder actionOrder, int passCount, Effect effect) {
        super(effect);
        _action = action;
        _cardTriggersUsed = cardTriggersUsed;
        _actionOrder = actionOrder;
        _passCount = passCount;
        _effect = effect;
        _actionsEnvironment = _game.getActionsEnvironment();
    }


    public void doPlayEffect() {
        final String activePlayer = _actionOrder.getNextPlayer();

        final List<Action> optionalBeforeTriggers =
                _actionsEnvironment.getOptionalBeforeTriggers(activePlayer, _effect);
        // Remove triggers already resolved
        optionalBeforeTriggers.removeIf(action -> _cardTriggersUsed.contains(action.getPerformingCard()));

        final List<Action> optionalBeforeActions =
                _actionsEnvironment.getOptionalBeforeActions(activePlayer, _effect);

        List<Action> possibleActions = new LinkedList<>(optionalBeforeTriggers);
        possibleActions.addAll(optionalBeforeActions);

        if (possibleActions.isEmpty()) {
            if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                Effect effect = new PlayOutOptionalBeforeResponsesEffect(_action, _cardTriggersUsed,
                        _actionOrder, _passCount + 1, _effect);
                _action.insertEffect(_game, new SubAction(_action, effect));
            }
        } else {
            Player decidingPlayer = _game.getGameState().getPlayer(activePlayer);
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(decidingPlayer,
                            _effect.getText() + " - Optional \"is about to\" responses", possibleActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            Action subAction = null;
                            if (action != null) {
                                _actionsEnvironment.addActionToStack(action);
                                if (optionalBeforeTriggers.contains(action))
                                    _cardTriggersUsed.add(action.getPerformingCard());
                                subAction = new SubAction(_action, new PlayOutOptionalBeforeResponsesEffect(
                                        _action, _cardTriggersUsed, _actionOrder, 0, _effect));
                            } else {
                                if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                                    subAction = new SubAction(_action, new PlayOutOptionalBeforeResponsesEffect(
                                            _action, _cardTriggersUsed, _actionOrder,
                                            _passCount + 1, _effect));
                                }
                            }
                            if (subAction != null)
                                _action.insertEffect(_game, subAction);
                        }
                    });
        }
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