package com.gempukku.stccg.game;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.turn.PlayOutEffectResults;
import com.gempukku.stccg.actions.turn.StackActionEffect;
import com.gempukku.stccg.gamestate.ActionsEnvironment;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

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
            Set<EffectResult> effectResults = actionsEnvironment.consumeEffectResults();
            effectResults.forEach(effectResult -> effectResult.createOptionalAfterTriggerActions(_game));
            if (effectResults.isEmpty()) {
                if (actionsEnvironment.hasNoActionsInProgress())
                    continueCurrentProcess();
                else
                    executeNextSubaction();
            } else {
                actionsEnvironment.addActionToStack(new PlayOutEffectResults(_game, effectResults));
            }
            _game.getGameState().updateGameStatsAndSendIfChanged();

            // Check if an unusually large number loops since user decision, which means game is probably in a loop
            if (numSinceDecision >= MAXIMUM_LOOPS)
                breakExcessiveLoop(numSinceDecision);
        }
    }

    private void continueCurrentProcess() {
        if (_currentGameProcess.isFinished()) {
            _currentGameProcess = _currentGameProcess.getNextProcess();
        } else {
            // TODO - This implementation seems to assume that game stats will never change during a process
            _currentGameProcess.process();
            _game.getGameState().updateGameStatsAndSendIfChanged();
            _currentGameProcess.finish();
        }
    }

    private void executeNextSubaction() {
        ActionsEnvironment actionsEnvironment = _game.getActionsEnvironment();
        Action action = actionsEnvironment.getCurrentAction();
        try {
            Effect effect;
            if (action instanceof ActionyAction actiony) {
                Action nextAction = actiony.nextAction(_game);
                effect = (nextAction == null) ? null : new StackActionEffect(_game, nextAction);
            } else {
                effect = action.nextEffect(_game);
            }
            if (effect == null) {
                actionsEnvironment.removeCompletedAction(action);
            } else {
                effect.playEffect();
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

        List<EffectResult> effectResults =
                _game.getActionsEnvironment().consumeEffectResults().stream().toList();
        effectResults.forEach(effectResult -> sendMessage(
                "EffectResult " + (effectResults.indexOf(effectResult) + 1) + ": " + effectResult.getType().name()));
        throw new UnsupportedOperationException(errorMessage);
    }

    private void sendMessage(String message) {
        _game.sendMessage(message);
    }
    public GameProcess getCurrentProcess() { return _currentGameProcess; }
    public void setCurrentProcess(GameProcess process) { _currentGameProcess = process; }

}