package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.TriggeringResultEffect;
import com.gempukku.stccg.actions.EffectType;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.actions.turn.EndOfTurnResult;
import com.gempukku.stccg.processes.GameProcess;

public class TribblesEndOfTurnGameProcess extends TribblesGameProcess {
    public TribblesEndOfTurnGameProcess(TribblesGame game) {
        super(game);
    }
    @Override
    public void process() {
//        game.sendMessage("DEBUG: Beginning TribblesEndOfTurnGameProcess");
        SystemQueueAction action = new SystemQueueAction(_game);
        action.setText("End of turn");
        action.appendEffect(
                new TriggeringResultEffect(_game, new EndOfTurnResult(_game), "End of turn"));
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
                    public String getPerformingPlayerId() { return null; }

                    @Override
                    public void playEffect() {
                        _game.getModifiersEnvironment().signalEndOfTurn();
                        _game.getActionsEnvironment().signalEndOfTurn();
                        _game.getGameState().stopAffectingCardsForCurrentPlayer();
                    }

                    @Override
                    public TribblesGame getGame() { return _game; }
                });
        boolean playerWentOut = false;
        for (String playerId : _game.getPlayerIds()) {
            if (_game.getGameState().getHand(playerId).isEmpty()) {
                playerWentOut = true;
            }
        }
        if (playerWentOut)
            _game.getGameState().endRound();
        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        if (_game.getGameState().isCurrentRoundOver())
            return new TribblesEndOfRoundGameProcess(_game);
        else return new TribblesBetweenTurnsProcess(_game);
    }
}