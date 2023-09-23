package com.gempukku.lotro.processes;

import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.SystemQueueAction;
import com.gempukku.lotro.effects.TriggeringResultEffect;
import com.gempukku.lotro.effects.UnrespondableEffect;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.results.StartOfTurnResult;

public class TribblesStartOfTurnGameProcess implements GameProcess {
    @Override
    public void process(DefaultGame game) {
        game.getGameState().startAffectingCardsForCurrentPlayer(game);

        SystemQueueAction action = new SystemQueueAction();

        action.appendEffect(new UnrespondableEffect() {
            @Override
            protected void doPlayEffect(DefaultGame game) {
                var state = game.getGameState();
                state.sendMessage("\n\n========\n\nStart of " + state.getCurrentPlayerId() + "'s turn.");
            }
        });

        action.appendEffect(
                new TriggeringResultEffect(new StartOfTurnResult(), "Start of turn"));
        ((ModifiersLogic) game.getModifiersEnvironment()).signalStartOfTurn(game.getGameState().getCurrentPlayerId());
        game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesTurnProcess();
    }
}
