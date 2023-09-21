package com.gempukku.lotro.processes;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.actions.SystemQueueAction;
import com.gempukku.lotro.effects.TriggeringResultEffect;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.effects.AbstractSuccessfulEffect;
import com.gempukku.lotro.results.EndOfTurnResult;

public class EndOfTurnGameProcess implements GameProcess {
    @Override
    public void process(DefaultGame game) {
        game.getGameState().sendMessage("DEBUG: Beginning EndOfTurnGameProcess");
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
                        game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
                        game.getGameState().sendMessage("End of turn, players swap roles.");
                    }
                });
        game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return new BetweenTurnsProcess();
    }
}
