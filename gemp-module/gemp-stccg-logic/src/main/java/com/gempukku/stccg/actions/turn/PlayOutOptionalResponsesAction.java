package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class PlayOutOptionalResponsesAction extends SystemQueueAction {
    private final ActionResult _actionResult;

    public PlayOutOptionalResponsesAction(DefaultGame game, ActionResult actionResult) {
        super(game);
        _actionResult = actionResult;
    }

    @Override
    public Action nextAction(DefaultGame cardGame)
            throws PlayerNotFoundException, InvalidGameLogicException, CardNotFoundException {
        final String activePlayerName = _actionResult.getNextRespondingPlayer();
        Player activePlayer = cardGame.getPlayer(activePlayerName);

        List<TopLevelSelectableAction> possibleActions = _actionResult.getOptionalAfterActions(cardGame, activePlayer);

        if (possibleActions.isEmpty()) {
            _actionResult.incrementPassCount();
            if (_actionResult.getPassCount() < _actionResult.getRespondingPlayerCount()) {
                _actionResult.addNextAction(
                        new PlayOutOptionalResponsesAction(cardGame, _actionResult));
            }
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(activePlayer, DecisionContext.SELECT_OPTIONAL_RESPONSE_ACTION,
                            possibleActions, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            try {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    cardGame.getActionsEnvironment().addActionToStack(action);
                                    _actionResult.markActionAsUsed(action, cardGame, activePlayer);
                                    _actionResult.setPassCount(0);
                                } else {
                                    _actionResult.incrementPassCount();
                                }
                                if (_actionResult.getPassCount() < _actionResult.getRespondingPlayerCount()) {
                                    _actionResult.addNextAction(new PlayOutOptionalResponsesAction(cardGame,
                                            _actionResult));
                                }
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