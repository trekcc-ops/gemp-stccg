package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ActionSelectionDecision;

import java.util.List;
import java.util.Set;

class PlayOutRequiredBeforeResponsesEffect extends UnrespondableEffect {
    private final SystemQueueAction _action;
    private final Set<PhysicalCard> _cardTriggersUsed;
    private final Effect _effect;
    private final ActionsEnvironment _actionsEnvironment;

    PlayOutRequiredBeforeResponsesEffect(SystemQueueAction action, Set<PhysicalCard> cardTriggersUsed,
                                         Effect effect) {
        super(effect.getGame());
        _action = action;
        _cardTriggersUsed = cardTriggersUsed;
        _effect = effect;
        _actionsEnvironment = _game.getActionsEnvironment();
    }


    @Override
    protected void doPlayEffect() {
        final List<Action> requiredBeforeTriggers = _actionsEnvironment.getRequiredBeforeTriggers(_effect);
        // Remove triggers already resolved
        requiredBeforeTriggers.removeIf(action -> _cardTriggersUsed.contains(action.getActionSource()));

        if (requiredBeforeTriggers.size() == 1) {
            _actionsEnvironment.addActionToStack(requiredBeforeTriggers.getFirst());
        } else if (requiredBeforeTriggers.size() > 1) {
            _game.getUserFeedback().sendAwaitingDecision(
                    new ActionSelectionDecision(_game.getCurrentPlayer(), _effect.getText() +
                            " - Required \"is about to\" responses", requiredBeforeTriggers) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _actionsEnvironment.addActionToStack(action);
                                if (requiredBeforeTriggers.contains(action))
                                    _cardTriggersUsed.add(action.getActionSource());
                                _actionsEnvironment.addActionToStack(action);
                                _action.insertEffect(new PlayOutRequiredBeforeResponsesEffect(
                                        _action, _cardTriggersUsed, _effect));
                            }
                        }
                    });
        }
    }
}