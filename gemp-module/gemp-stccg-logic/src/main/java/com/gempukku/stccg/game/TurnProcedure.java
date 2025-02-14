package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.turn.PlayOutEffectResults;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;
import java.util.Set;
import java.util.Stack;

public class TurnProcedure {

    private static final int MAXIMUM_LOOPS = 5000; // Max number of loops allowed before throwing error
    private final DefaultGame _game;


    public TurnProcedure(DefaultGame game) {
        _game = game;
    }


    public void carryOutPendingActionsUntilDecisionNeeded() throws PlayerNotFoundException, InvalidGameLogicException,
            CardNotFoundException, InvalidGameOperationException {
        int numSinceDecision = 0;
        ActionsEnvironment actionsEnvironment = _game.getActionsEnvironment();

        while (_game.isCarryingOutEffects()) {
            numSinceDecision++;
            // First check for any "state-based" effects
            Set<ActionResult> actionResults = actionsEnvironment.consumeEffectResults();
            for (ActionResult result : actionResults) {
                result.createOptionalAfterTriggerActions(_game);
            }
            if (actionResults.isEmpty()) {
                if (actionsEnvironment.hasNoActionsInProgress())
                    try {
                        continueCurrentProcess();
                    } catch(InvalidGameLogicException exp) {
                        _game.sendErrorMessage(exp);
                    }
                else
                    executeNextSubaction();
            } else {
                actionsEnvironment.addActionToStack(new PlayOutEffectResults(_game, actionResults));
            }
            _game.updateGameStatsAndSendIfChanged();

            // Check if an unusually large number loops since user decision, which means game is probably in a loop
            if (numSinceDecision >= MAXIMUM_LOOPS)
                breakExcessiveLoop(numSinceDecision);
        }
    }

    private void continueCurrentProcess() throws InvalidGameLogicException, PlayerNotFoundException {
        GameState gameState = _game.getGameState();
        GameProcess originalProcess = gameState.getCurrentProcess();
        if (originalProcess.isFinished()) {
            gameState.setCurrentProcess(originalProcess.getNextProcess(_game));
        } else {
            // TODO - This implementation seems to assume that game stats will never change during a process
            originalProcess.process(_game);
            _game.updateGameStatsAndSendIfChanged();
            originalProcess.setFinished(true);
        }
    }

    private void executeNextSubaction() throws PlayerNotFoundException, InvalidGameLogicException,
            CardNotFoundException, InvalidGameOperationException {
        ActionsEnvironment actionsEnvironment = _game.getActionsEnvironment();
        Action currentAction = actionsEnvironment.getCurrentAction();
        Action nextAction = currentAction.nextAction(_game);

        if (currentAction.isInProgress() && nextAction != null) {
            _game.getActionsEnvironment().addActionToStack(nextAction);
        } else if (currentAction.wasCompleted()) {
            actionsEnvironment.removeCompletedActionFromStack(currentAction);
            ActionType actionType = currentAction.getActionType();
            switch(actionType) {
                case CHANGE_AFFILIATION, DISCARD, REMOVE_CARD_FROM_GAME, SCORE_POINTS, SEED_CARD, STOP_CARDS:
                    _game.sendActionResultToClient();
                    break;
                default:
                    break;
            }
        } else if (_game.isCarryingOutEffects()) {
            throw new InvalidGameLogicException("Unable to process action " + currentAction.getActionId() +
                    " of type " + currentAction.getClass().getSimpleName());
        }
    }

    private void breakExcessiveLoop(int numSinceDecision) {
        String errorMessage = "There's been " + numSinceDecision +
                " actions/effects since last user decision. Game is probably looping, so ending game.";
        sendMessage(errorMessage);

        Stack<Action> actionStack = _game.getActionsEnvironment().getActionStack();
        sendMessage("Action stack size: " + actionStack.size());
        actionStack.forEach(action -> sendMessage("Action " + (actionStack.indexOf(action) + 1) + ": " +
                action.getClass().getSimpleName()));

        List<ActionResult> actionResults =
                _game.getActionsEnvironment().consumeEffectResults().stream().toList();
        actionResults.forEach(effectResult -> sendMessage(
                "EffectResult " + (actionResults.indexOf(effectResult) + 1) + ": " + effectResult.getType().name()));
        throw new UnsupportedOperationException(errorMessage);
    }

    private void sendMessage(String message) {
        _game.sendMessage(message);
    }

}