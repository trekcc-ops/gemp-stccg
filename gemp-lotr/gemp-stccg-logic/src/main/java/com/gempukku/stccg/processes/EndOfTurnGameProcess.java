package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.TriggeringResultEffect;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.turn.EndOfTurnResult;

public class EndOfTurnGameProcess extends GameProcess {
    private final DefaultGame _game;
    public EndOfTurnGameProcess(DefaultGame game) {
        _game = game;
    }
    @Override
    public void process() {
        _game.getGameState().sendMessage("DEBUG: Beginning EndOfTurnGameProcess");
        SystemQueueAction action = new SystemQueueAction(_game);
        action.setText("End of turn");
        action.appendEffect(
                new TriggeringResultEffect(null, new EndOfTurnResult(_game), "End of turn"));
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
                        _game.getModifiersEnvironment().signalEndOfTurn();
                        _game.getActionsEnvironment().signalEndOfTurn();
                        _game.getGameState().stopAffectingCardsForCurrentPlayer();
                        _game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
                        _game.getGameState().sendMessage("End of turn, players swap roles.");
                    }
                });
        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return new BetweenTurnsProcess(_game, new StartOfPhaseGameProcess(Phase.PLAY, null, _game));
    }
}
