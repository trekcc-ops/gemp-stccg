package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.game.*;

import java.util.*;

public class TurnProcedure implements Snapshotable<TurnProcedure> {
    private static final int MAXIMUM_LOOPS = 5000; // Max number of loops allowed before throwing error
    private final DefaultGame _game;
    private GameProcess _currentGameProcess;
    private boolean _playedGameProcess;
    private final ActionsEnvironment _actionsEnvironment;

    @Override
    public TurnProcedure generateSnapshot(SnapshotData snapshotData) {
        ActionsEnvironment actionsEnvironment = snapshotData.getDataForSnapshot(_actionsEnvironment);
        return new TurnProcedure(_game, _currentGameProcess, _playedGameProcess, actionsEnvironment);
    }


    public TurnProcedure(DefaultGame game, GameProcess currentProcess) {
        this(game, currentProcess, false, game.getActionsEnvironment());
    }


    private TurnProcedure(DefaultGame game, GameProcess currentProcess, boolean playedGameProcess,
                          ActionsEnvironment actionsEnvironment) {
        _game = game;
        _actionsEnvironment = actionsEnvironment;
        _currentGameProcess = currentProcess;
        _playedGameProcess = playedGameProcess;
    }

    public void carryOutPendingActionsUntilDecisionNeeded() {
        int numSinceDecision = 0;

        while (_game.isCarryingOutEffects()) {
            numSinceDecision++;
            // First check for any "state-based" effects
            Set<EffectResult> effectResults = _actionsEnvironment.consumeEffectResults();
            effectResults.forEach(EffectResult::createOptionalAfterTriggerActions);
            if (effectResults.isEmpty()) {
                if (_actionsEnvironment.getActionStack().isEmpty())
                    continueCurrentProcess();
                else
                    executeNextAction();
            } else {
                _actionsEnvironment.addActionToStack(new PlayOutEffectResults(_game, effectResults));
            }
            _game.getGameState().updateGameStatsAndSendIfChanged();

            // Check if an unusually large number loops since user decision, which means game is probably in a loop
            if (numSinceDecision >= MAXIMUM_LOOPS)
                breakExcessiveLoop(numSinceDecision);
        }
    }

    private void continueCurrentProcess() {
        if (_playedGameProcess) {
            _currentGameProcess = _currentGameProcess.getNextProcess();
            _playedGameProcess = false;
        } else {
            _currentGameProcess.process();
            _game.getGameState().updateGameStatsAndSendIfChanged();
            _playedGameProcess = true;
        }
    }

    private void executeNextAction() {
        Stack<Action> actionStack = _actionsEnvironment.getActionStack();
        Action action = actionStack.peek();
        try {
            Effect effect = action.nextEffect();
            if (effect == null) {
                actionStack.remove(actionStack.lastIndexOf(action));
            } else {
                if (effect.getType() == null) {
                    effect.playEffect();
                } else
                    actionStack.add(new PlayOutEffect(_game, effect));
            }
        } catch (InvalidGameLogicException exp) {
            sendMessage(exp.getMessage());
        }
    }

    private void breakExcessiveLoop(int numSinceDecision) {
        String errorMessage = "There's been " + numSinceDecision +
                " actions/effects since last user decision. Game is probably looping, so ending game.";
        sendMessage(errorMessage);

        Stack<Action> actionStack = _actionsEnvironment.getActionStack();
        sendMessage("Action stack size: " + actionStack.size());
        actionStack.forEach(action -> sendMessage("Action " + (actionStack.indexOf(action) + 1) + ": " +
                action.getClass().getSimpleName() + (action.getActionSource() != null ?
                " Source: " + action.getActionSource().getFullName() : "")));

        List<EffectResult> effectResults =
                _game.getActionsEnvironment().consumeEffectResults().stream().toList();
        effectResults.forEach(effectResult -> sendMessage(
                "EffectResult " + (effectResults.indexOf(effectResult) + 1) + ": " + effectResult.getType().name()));
        throw new UnsupportedOperationException(errorMessage);
    }

    private void sendMessage(String message) {
        _game.sendMessage(message);
    }

}