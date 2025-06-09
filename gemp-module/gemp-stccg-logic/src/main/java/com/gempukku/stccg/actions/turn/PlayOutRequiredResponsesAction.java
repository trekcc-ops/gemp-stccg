package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.ActionsEnvironment;

import java.util.List;

public final class PlayOutRequiredResponsesAction extends SystemQueueAction {
    private final ActionResult _actionResult;
    private final List<TopLevelSelectableAction> _responses;

    public PlayOutRequiredResponsesAction(DefaultGame game, ActionResult actionResult,
                                          List<TopLevelSelectableAction> responses) {
        super(game);
        _actionResult = actionResult;
        _responses = responses;
    }



    @Override
    protected void processEffect(DefaultGame cardGame)
            throws CardNotFoundException, PlayerNotFoundException, InvalidGameLogicException {
        ActionsEnvironment environment = cardGame.getActionsEnvironment();
        if (_responses.size() == 1) {
            environment.addActionToStack(_responses.getFirst());
        } else if (areAllActionsTheSame(_responses)) {
            Action anyAction = _responses.removeFirst();
            environment.addActionToStack(anyAction);
            _actionResult.addNextAction(new PlayOutRequiredResponsesAction(cardGame, _actionResult, _responses));
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new ActionSelectionDecision(cardGame.getCurrentPlayer(),
                            DecisionContext.SELECT_REQUIRED_RESPONSE_ACTION, _responses, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            try {
                                Action action = getSelectedAction(result);
                                environment.addActionToStack(action);
                                _responses.remove(action);
                                _actionResult.addNextAction(
                                        new PlayOutRequiredResponsesAction(cardGame, _actionResult, _responses));
                            } catch(InvalidGameLogicException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }

                        @Override
                        public String[] getCardIds() {
                            return getDecisionParameters().get("cardId");
                        }


                    });
        }
        setAsSuccessful();
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
}