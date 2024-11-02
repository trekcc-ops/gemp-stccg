package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.game.*;

import java.util.*;

public class TurnProcedure implements Snapshotable<TurnProcedure> {
    private static final int MAXIMUM_LOOPS = 5000; // Max number of loops allowed before throwing error
    private final DefaultGame _game;
    private final Stack<Action> _actionStack;
    private GameProcess _gameProcess;
    private boolean _playedGameProcess;
    private final ActionsEnvironment _actionsEnvironment;
    private final GameProcess _firstProcess;

    @Override
    public TurnProcedure generateSnapshot(SnapshotData snapshotData) {
        ActionsEnvironment actionsEnvironment = snapshotData.getDataForSnapshot(_actionsEnvironment);
        return new TurnProcedure(_game, _gameProcess, _playedGameProcess, actionsEnvironment, _firstProcess);
    }


    public TurnProcedure(DefaultGame game, GameProcess firstProcess) {
        this(game, null, false, game.getActionsEnvironment(), firstProcess);
    }


    private TurnProcedure(DefaultGame game, GameProcess currentProcess, boolean playedGameProcess,
                          ActionsEnvironment actionsEnvironment, GameProcess firstProcess) {
        _game = game;
        _actionsEnvironment = actionsEnvironment;
        _actionStack = actionsEnvironment.getActionStack();
        _gameProcess = currentProcess;
        _playedGameProcess = playedGameProcess;
        _firstProcess = firstProcess;
    }

    public void carryOutPendingActionsUntilDecisionNeeded() {
        int numSinceDecision = 0;

        if (_gameProcess == null) {
            // Take game snapshot for start of game
            _gameProcess = _firstProcess;
        }

        while (_game.isCarryingOutEffects()) {
            numSinceDecision++;
            // First check for any "state-based" effects
            Set<EffectResult> effectResults = _actionsEnvironment.consumeEffectResults();
            effectResults.forEach(EffectResult::createOptionalAfterTriggerActions);
            if (effectResults.isEmpty()) {
                if (_actionStack.isEmpty())
                    continueCurrentProcess();
                else
                    executeNextAction();
            } else {
                _actionStack.add(new PlayOutEffectResults(_game, effectResults));
            }
            _game.getGameState().updateGameStatsAndSendIfChanged();

            // Check if an unusually large number loops since user decision, which means game is probably in a loop
            if (numSinceDecision >= MAXIMUM_LOOPS)
                breakExcessiveLoop(numSinceDecision);
        }
    }

    private void continueCurrentProcess() {
        if (_playedGameProcess) {
            _gameProcess = _gameProcess.getNextProcess();
            _playedGameProcess = false;
        } else {
            _gameProcess.process();
            _game.getGameState().updateGameStatsAndSendIfChanged();
            _playedGameProcess = true;
        }
    }

    private void executeNextAction() {
        Action action = _actionStack.peek();
        try {
            Effect effect = action.nextEffect();
            if (effect == null) {
                _actionStack.remove(_actionStack.lastIndexOf(action));
            } else {
                if (effect.getType() == null) {
                    effect.playEffect();
                } else
                    _actionStack.add(new PlayOutEffect(_game, effect));
            }
        } catch (InvalidGameLogicException exp) {
            sendMessage(exp.getMessage());
        }
    }

    private void breakExcessiveLoop(int numSinceDecision) {
        String errorMessage = "There's been " + numSinceDecision +
                " actions/effects since last user decision. Game is probably looping, so ending game.";
        sendMessage(errorMessage);

        int actionNum = 1;
        sendMessage("Action stack size: " + _actionStack.size());
        for (Action action : _actionStack) {
            sendMessage("Action " + (actionNum) + ": " +
                    action.getClass().getSimpleName() + (action.getActionSource() != null ?
                    " Source: " + action.getActionSource().getFullName() : ""));
            actionNum++;
        }

        Set<EffectResult> effectResults = _game.getActionsEnvironment().consumeEffectResults();
        int numEffectResult = 1;
        for (EffectResult effectResult : effectResults) {
            String message = "EffectResult " + numEffectResult + ": " + effectResult.getType().name();
            sendMessage(message);
            numEffectResult++;
        }
        throw new UnsupportedOperationException(errorMessage);
    }

    private void sendMessage(String message) {
        _game.sendMessage(message);
    }

}