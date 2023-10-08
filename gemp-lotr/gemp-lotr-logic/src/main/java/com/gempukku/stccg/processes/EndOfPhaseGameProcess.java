package com.gempukku.stccg.processes;

import com.gempukku.stccg.common.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.effects.AbstractSuccessfulEffect;
import com.gempukku.stccg.results.EndOfPhaseResult;
import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.effects.TriggeringResultEffect;
import com.gempukku.stccg.modifiers.ModifiersLogic;

public class EndOfPhaseGameProcess implements GameProcess {
    private final Phase _phase;
    private final GameProcess _followingGameProcess;

    public EndOfPhaseGameProcess(Phase phase, GameProcess followingGameProcess) {
        _phase = phase;
        _followingGameProcess = followingGameProcess;
    }

    @Override
    public void process(DefaultGame game) {
        game.getGameState().sendMessage("DEBUG: Beginning EndOfPhaseGameProcess");
        SystemQueueAction action = new SystemQueueAction();
        action.setText("End of " + _phase + " phase");
        action.appendEffect(
                new TriggeringResultEffect(null, new EndOfPhaseResult(_phase), "End of " + _phase + " phase"));
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
                        ((ModifiersLogic) game.getModifiersEnvironment()).signalEndOfPhase(_phase);
                        ((DefaultActionsEnvironment) game.getActionsEnvironment()).signalEndOfPhase(_phase);
                        game.getGameState().sendMessage("End of " + _phase + " phase.");
                    }
                });
        game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
