package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.actions.turn.EndOfPhaseResult;
import com.gempukku.stccg.actions.turn.TriggeringResultEffect;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class EndOfPhaseGameProcess extends GameProcess {
    private final Phase _phase;
    private final GameProcess _followingGameProcess;
    private final DefaultGame _game;

    public EndOfPhaseGameProcess(DefaultGame game, GameProcess followingGameProcess) {
        _phase = game.getGameState().getCurrentPhase();
        _game = game;
        _followingGameProcess = followingGameProcess;
    }

    @Override
    public void process() {
        SystemQueueAction action = new SystemQueueAction(_game);
        action.setText("End of " + _phase + " phase");
        action.appendEffect(
                new TriggeringResultEffect(new EndOfPhaseResult(_phase, _game), "End of " + _phase + " phase"));
        action.appendEffect(
                new Effect() {
                    @Override
                    public String getText() {
                        return null;
                    }
                    @Override
                    public String getPerformingPlayerId() { return null; }
                    @Override
                    public boolean isPlayableInFull() {
                        return true;
                    }

                    @Override
                    public boolean wasCarriedOut() {
                        return true;
                    }

                    @Override
                    public EffectType getType() {
                        return null;
                    }

                    @Override
                    public void playEffect() {
                        _game.getModifiersEnvironment().signalEndOfPhase();
                        _game.getActionsEnvironment().signalEndOfPhase();
                        _game.sendMessage("End of " + _phase + " phase.");
                    }

                    public DefaultGame getGame() { return _game; }
                });
        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
