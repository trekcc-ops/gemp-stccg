package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlayOutOptionalResponsesAction extends SystemQueueAction {
    private final PlayOutEffectResults _action;
    private final ActionOrder _actionOrder;
    private final int _passCount;
    private final ActionsEnvironment _actionsEnvironment;
    private final ActionResult _actionResult;

    public PlayOutOptionalResponsesAction(DefaultGame game, PlayOutEffectResults action, ActionOrder actionOrder,
                                          int passCount, ActionResult actionResult) {
        super(game);
        _action = action;
        _actionOrder = actionOrder;
        _passCount = passCount;
        _actionResult = actionResult;
        _actionsEnvironment = game.getActionsEnvironment();
    }

    @Override
    public Action nextAction(DefaultGame cardGame)
            throws PlayerNotFoundException, InvalidGameLogicException, CardNotFoundException {
        final String activePlayerName = _actionOrder.getNextPlayer();
        Player activePlayer = cardGame.getPlayer(activePlayerName);

        final Map<TopLevelSelectableAction, ActionResult> optionalAfterTriggers =
                _actionsEnvironment.getOptionalAfterTriggers(cardGame, activePlayer, List.of(_actionResult));

        List<TopLevelSelectableAction> possibleActions = new LinkedList<>(optionalAfterTriggers.keySet());
        possibleActions.addAll(_actionsEnvironment.getOptionalAfterActions(cardGame, activePlayer, List.of(_actionResult)));

        if (possibleActions.isEmpty()) {
            if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                _action.insertEffect(new PlayOutOptionalResponsesAction(cardGame,
                        _action, _actionOrder, _passCount + 1, _actionResult));
            }
        } else {
            Player decidingPlayer = cardGame.getGameState().getPlayer(activePlayerName);
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(decidingPlayer, DecisionContext.SELECT_OPTIONAL_RESPONSE_ACTION,
                            possibleActions, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            try {
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
                                    _action.insertEffect(new PlayOutOptionalResponsesAction(cardGame,
                                            _action, _actionOrder, nextPassCount, _actionResult));
                            } catch(InvalidGameLogicException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }
                    });
        }
        Action nextAction = getNextAction();
        if (nextAction == null)
            processEffect(cardGame);
        return nextAction;
    }

}