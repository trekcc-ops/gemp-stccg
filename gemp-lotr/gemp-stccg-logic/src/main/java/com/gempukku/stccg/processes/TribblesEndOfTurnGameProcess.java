package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.actions.DefaultActionsEnvironment;
import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.effects.AbstractSuccessfulEffect;
import com.gempukku.stccg.effects.TriggeringResultEffect;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.results.EndOfTurnResult;

public class TribblesEndOfTurnGameProcess extends DefaultGameProcess<TribblesGame> {
    private GameProcess _nextProcess;
    @Override
    public void process(TribblesGame game) {
//        game.getGameState().sendMessage("DEBUG: Beginning TribblesEndOfTurnGameProcess");
        SystemQueueAction action = new SystemQueueAction();
        action.setText("End of turn");
        action.appendEffect(
                new TriggeringResultEffect(null, new EndOfTurnResult(), "End of turn"));
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
                        ((ModifiersLogic) game.getModifiersEnvironment()).signalEndOfTurn();
                        ((DefaultActionsEnvironment) game.getActionsEnvironment()).signalEndOfTurn();
                        game.getGameState().stopAffectingCardsForCurrentPlayer();
                    }
                });
        boolean playerWentOut = false;
        for (String playerId : game.getPlayers()) {
            if (game.getGameState().getHand(playerId).size() == 0) {
                playerWentOut = true;
            }
        }
        if (playerWentOut) {
            _nextProcess = new TribblesEndOfRoundGameProcess();
        } else {
            _nextProcess = new TribblesBetweenTurnsProcess();
        }
        game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
