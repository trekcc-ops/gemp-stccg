package com.gempukku.lotro.game.timing.processes.turn.tribbles;

import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.actions.lotronly.SystemQueueAction;
import com.gempukku.lotro.game.effects.TriggeringResultEffect;
import com.gempukku.lotro.game.effects.UnrespondableEffect;
import com.gempukku.lotro.game.timing.processes.GameProcess;
import com.gempukku.lotro.game.timing.results.StartOfTurnResult;

public class TribblesStartOfTurnGameProcess implements GameProcess {
    @Override
    public void process(DefaultGame game) {
        game.getGameState().sendMessage("DEBUG: Beginning StartOfTurnGameProcess");
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

        game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesTurnProcess();
    }
}