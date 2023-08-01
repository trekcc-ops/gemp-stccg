package com.gempukku.lotro.processes.turn;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.effects.AbstractSuccessfulEffect;
import com.gempukku.lotro.effects.results.EndOfPhaseResult;
import com.gempukku.lotro.actions.lotronly.SystemQueueAction;
import com.gempukku.lotro.effects.TriggeringResultEffect;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.processes.GameProcess;

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