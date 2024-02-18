package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.TriggeringResultEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.results.StartOfPhaseResult;

public class StartOfPhaseGameProcess extends GameProcess {
    private final Phase _phase;
    private String _playerId;
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
        SystemQueueAction action = new SystemQueueAction();
        action.setText("Start of " + _phase + " phase");
        action.appendEffect(
                new TriggeringResultEffect(null, new StartOfPhaseResult(_phase, _playerId), "Start of " + _phase + " phase"));
        action.appendEffect(
                new Effect() {
                    @Override
                    public String getText() {
                        return null;
                    }

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
                        _game.getGameState().sendMessage("\nStart of " + _phase + " phase.");
                    }
                });

        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
