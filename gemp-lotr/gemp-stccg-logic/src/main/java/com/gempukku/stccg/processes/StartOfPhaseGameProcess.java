package com.gempukku.stccg.processes;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.effects.AbstractSuccessfulEffect;
import com.gempukku.stccg.results.StartOfPhaseResult;
import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.effects.TriggeringResultEffect;
import com.gempukku.stccg.modifiers.ModifiersLogic;

public class StartOfPhaseGameProcess implements GameProcess {
    private final Phase _phase;
    private String _playerId;
    private final GameProcess _followingGameProcess;

    public StartOfPhaseGameProcess(Phase phase, GameProcess followingGameProcess) {
        _phase = phase;
        _followingGameProcess = followingGameProcess;
    }

    public StartOfPhaseGameProcess(Phase phase, String playerId, GameProcess followingGameProcess) {
        _phase = phase;
        _playerId = playerId;
        _followingGameProcess = followingGameProcess;
    }

    @Override
    public void process(DefaultGame game) {
        game.getGameState().setCurrentPhase(_phase);
        SystemQueueAction action = new SystemQueueAction();
        action.setText("Start of " + _phase + " phase");
        action.appendEffect(
                new TriggeringResultEffect(null, new StartOfPhaseResult(_phase, _playerId), "Start of " + _phase + " phase"));
        action.appendEffect(
                new AbstractSuccessfulEffect() {
                    @Override
                    public String getText(DefaultGame game) {
                        return null;
                    }

                    @Override
                    public Type getType() {
                        return null;
                    }

                    @Override
                    public void playEffect(DefaultGame game) {
                        ((ModifiersLogic) game.getModifiersEnvironment()).signalStartOfPhase(_phase);
                        ((DefaultActionsEnvironment) game.getActionsEnvironment()).signalStartOfPhase(_phase);
                        game.getGameState().sendMessage("\nStart of " + _phase + " phase.");
                    }
                });

        game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
