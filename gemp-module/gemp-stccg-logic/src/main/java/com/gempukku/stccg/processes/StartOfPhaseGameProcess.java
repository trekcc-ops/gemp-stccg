package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.TriggeringResultEffect;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.actions.turn.StartOfPhaseResult;

public class StartOfPhaseGameProcess extends GameProcess {
    private final Phase _phase;
    private final String _playerId;
    private final GameProcess _followingGameProcess;
    private final DefaultGame _game;

    public StartOfPhaseGameProcess(Phase phase, GameProcess followingGameProcess, DefaultGame game) {
        this(phase, null, followingGameProcess, game);
    }

    public StartOfPhaseGameProcess(Phase phase, String playerId, GameProcess followingGameProcess, DefaultGame game) {
        _phase = phase;
        _playerId = playerId;
        _followingGameProcess = followingGameProcess;
        _game = game;
    }

    @Override
    public void process() {
        _game.getGameState().setCurrentPhase(_phase);
        SystemQueueAction action = new SystemQueueAction(_game);
        action.setText("Start of " + _phase + " phase");
        action.appendEffect(
                new TriggeringResultEffect(new StartOfPhaseResult(_phase, _playerId, _game), "Start of " + _phase + " phase"));
        action.appendEffect(
                new Effect() {
                    @Override
                    public String getText() {
                        return null;
                    }

                    @Override
                    public String getPerformingPlayerId() { return null; }

                    @Override
                    public EffectType getType() {
                        return null;
                    }
                    @Override
                    public boolean isPlayableInFull() {
                        return true;
                    }

                    @Override
                    public boolean wasCarriedOut() {
                        return true;
                    }

                    @Override
                    public void playEffect() {
                        ((ModifiersLogic) _game.getModifiersEnvironment()).signalStartOfPhase(_phase);
                        ((DefaultActionsEnvironment) _game.getActionsEnvironment()).signalStartOfPhase(_phase);
                        _game.sendMessage("\nStart of " + _phase + " phase.");
                    }

                    @Override
                    public DefaultGame getGame() { return _game; }
                });

        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
