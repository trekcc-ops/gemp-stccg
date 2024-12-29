package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.turn.PlayOutEffectResults;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;
import java.util.Set;
import java.util.Stack;

public class TurnProcedure implements Snapshotable<TurnProcedure> {
    private static final int MAXIMUM_LOOPS = 5000; // Max number of loops allowed before throwing error
    private final DefaultGame _game;
    private GameProcess _currentGameProcess;

    @Override
    public TurnProcedure generateSnapshot(SnapshotData snapshotData) {
        return new TurnProcedure(_game, _currentGameProcess);
    }


    public TurnProcedure(DefaultGame game, GameProcess currentProcess) {
        _game = game;
        _currentGameProcess = currentProcess;
    }


    public void carryOutPendingActionsUntilDecisionNeeded() {
        int numSinceDecision = 0;
        ActionsEnvironment actionsEnvironment = _game.getActionsEnvironment();

        while (_game.isCarryingOutEffects()) {
            numSinceDecision++;
            // First check for any "state-based" effects
            Set<ActionResult> actionResults = actionsEnvironment.consumeEffectResults();
            actionResults.forEach(effectResult -> effectResult.createOptionalAfterTriggerActions(_game));
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
            _game.getGameState().updateGameStatsAndSendIfChanged();

            // Check if an unusually large number loops since user decision, which means game is probably in a loop
            if (numSinceDecision >= MAXIMUM_LOOPS)
                breakExcessiveLoop(numSinceDecision);
        }
    }

    private void continueCurrentProcess() throws InvalidGameLogicException {
        if (_currentGameProcess.isFinished()) {
            _currentGameProcess = _currentGameProcess.getNextProcess(_game);
        } else {
            // TODO - This implementation seems to assume that game stats will never change during a process
            _currentGameProcess.process(_game);
            _game.getGameState().updateGameStatsAndSendIfChanged();
            _currentGameProcess.finish();
        }
    }

    private void executeNextSubaction() {
        ActionsEnvironment actionsEnvironment = _game.getActionsEnvironment();
        Action action = actionsEnvironment.getCurrentAction();
        try {
            Action nextAction = action.nextAction(_game);
            if (nextAction == null) {
                actionsEnvironment.removeCompletedActionFromStack(action);
            } else {
                _game.getActionsEnvironment().addActionToStack(nextAction);
            }
        } catch (InvalidGameLogicException exp) {
            _game.sendErrorMessage(exp);
        }
    }

    private void breakExcessiveLoop(int numSinceDecision) {
        String errorMessage = "There's been " + numSinceDecision +
                " actions/effects since last user decision. Game is probably looping, so ending game.";
        sendMessage(errorMessage);

        Stack<Action> actionStack = _game.getActionsEnvironment().getActionStack();
        sendMessage("Action stack size: " + actionStack.size());
        actionStack.forEach(action -> sendMessage("Action " + (actionStack.indexOf(action) + 1) + ": " +
                action.getClass().getSimpleName() + (action.getPerformingCard() != null ?
                " Source: " + action.getPerformingCard().getFullName() : "")));

        List<ActionResult> actionResults =
                _game.getActionsEnvironment().consumeEffectResults().stream().toList();
        actionResults.forEach(effectResult -> sendMessage(
                "EffectResult " + (actionResults.indexOf(effectResult) + 1) + ": " + effectResult.getType().name()));
        throw new UnsupportedOperationException(errorMessage);
    }

    private void sendMessage(String message) {
        _game.sendMessage(message);
    }
    public GameProcess getCurrentProcess() { return _currentGameProcess; }
    public void setCurrentProcess(GameProcess process) { _currentGameProcess = process; }

}