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
            _game.sendActionResultToClient();

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
            _game.sendActionResultToClient();
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
            _game.sendActionResultToClient();
        } else if (_game.isCarryingOutEffects()) {
            throw new InvalidGameLogicException("Unable to process action " + currentAction.getActionId() +
                    " of type " + currentAction.getClass().getSimpleName());
        }
    }

    private void breakExcessiveLoop(int numSinceDecision) {
        String errorMessage = "There's been " + numSinceDecision +
                " actions/effects since last user decision. Game is probably looping, so ending game.";
        _game.sendErrorMessage(errorMessage);

        Stack<Action> actionStack = _game.getActionsEnvironment().getActionStack();
        _game.sendErrorMessage("Action stack size: " + actionStack.size());
        actionStack.forEach(action -> _game.sendErrorMessage("Action " + (actionStack.indexOf(action) + 1) + ": " +
                action.getClass().getSimpleName()));

        List<ActionResult> actionResults =
                _game.getActionsEnvironment().consumeEffectResults().stream().toList();
        actionResults.forEach(effectResult -> _game.sendErrorMessage(
                "EffectResult " + (actionResults.indexOf(effectResult) + 1) + ": " + effectResult.getType().name()));
        throw new UnsupportedOperationException(errorMessage);
    }

}