package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
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
    private final Collection<ActionResult> _actionResults;
    private final ActionsEnvironment _actionsEnvironment;

    public PlayOutOptionalResponsesAction(DefaultGame game, PlayOutEffectResults action, ActionOrder actionOrder,
                                          int passCount, Collection<ActionResult> actionResults) {
        super(game);
        _action = action;
        _actionOrder = actionOrder;
        _passCount = passCount;
        _actionResults = actionResults;
        _actionsEnvironment = game.getActionsEnvironment();
    }

    @Override
    public Action nextAction(DefaultGame cardGame)
            throws PlayerNotFoundException, InvalidGameLogicException, CardNotFoundException {
        final String activePlayerName = _actionOrder.getNextPlayer();
        Player activePlayer = cardGame.getPlayer(activePlayerName);

        final Map<TopLevelSelectableAction, ActionResult> optionalAfterTriggers =
                _actionsEnvironment.getOptionalAfterTriggers(cardGame, activePlayer, _actionResults);

        List<TopLevelSelectableAction> possibleActions = new LinkedList<>(optionalAfterTriggers.keySet());
        possibleActions.addAll(_actionsEnvironment.getOptionalAfterActions(cardGame, activePlayer, _actionResults));

        if (possibleActions.isEmpty()) {
            if ((_passCount + 1) < _actionOrder.getPlayerCount()) {
                _action.insertEffect(new PlayOutOptionalResponsesAction(cardGame,
                        _action, _actionOrder, _passCount + 1, _actionResults));
            }
        } else {
            Player decidingPlayer = cardGame.getGameState().getPlayer(activePlayerName);
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(decidingPlayer, "Optional responses", possibleActions,
                            cardGame) {
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
                                            _action, _actionOrder, nextPassCount, _actionResults));
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

    public Collection<ActionResult> getEffectResults() { return _actionResults; }

}