package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.turn.AllowResponsesAction;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
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
        action.appendAction(new AllowResponsesAction(_game, EffectResult.Type.END_OF_TURN));
        action.appendAction(new SystemQueueAction(_game) {
                    @Override
                    public Action nextAction(DefaultGame cardGame) {
                        _game.getModifiersEnvironment().signalEndOfTurn();
                        _game.getActionsEnvironment().signalEndOfTurn();
                        _game.getGameState().stopAffectingCardsForCurrentPlayer();
                        return getNextAction();
                    }
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