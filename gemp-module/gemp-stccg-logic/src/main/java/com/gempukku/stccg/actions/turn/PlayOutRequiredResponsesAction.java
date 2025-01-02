package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.ActionsEnvironment;

import java.util.List;

public final class PlayOutRequiredResponsesAction extends SystemQueueAction {
    private final PlayOutEffectResults _action;
    private final List<TopLevelSelectableAction> _responses;
    private boolean _initialized;

    public PlayOutRequiredResponsesAction(DefaultGame game, PlayOutEffectResults action,
                                          List<TopLevelSelectableAction> responses) {
        super(game);
        _action = action;
        _responses = responses;
    }


    public void doPlayEffect(DefaultGame cardGame) throws CardNotFoundException {
        ActionsEnvironment environment = cardGame.getActionsEnvironment();
        if (_responses.size() == 1) {
            environment.addActionToStack(_responses.getFirst());
        } else if (areAllActionsTheSame(_responses)) {
            Action anyAction = _responses.removeFirst();
            environment.addActionToStack(anyAction);
            _action.insertEffect(new PlayOutRequiredResponsesAction(cardGame, _action, _responses));
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new ActionSelectionDecision(cardGame.getCurrentPlayer(), "Required responses", _responses) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            environment.addActionToStack(action);
                            _responses.remove(action);
                            _action.insertEffect(
                                    new PlayOutRequiredResponsesAction(cardGame, _action, _responses));
                        }
                    });
        }
    }

    private static boolean areAllActionsTheSame(List<TopLevelSelectableAction> actions) {
        boolean result = true;
        TopLevelSelectableAction firstAction = actions.getFirst();
        if (firstAction.getPerformingCard() == null)
                result = false;
        for (TopLevelSelectableAction action : actions) {
            if (action.getPerformingCard() == null)
                result = false;
            else if (action.getPerformingCard().getBlueprint() != firstAction.getPerformingCard().getBlueprint())
                result = false;
        }
        return result;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws CardNotFoundException {
        if (!_initialized) {
            _initialized = true;
            doPlayEffect(cardGame);
        }
        return getNextAction();
    }
}