package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.effects.TriggeringResultEffect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.results.StartOfTurnResult;

import java.util.Objects;

public class StartOfTurnGameProcess implements GameProcess {
    private GameProcess _followingGameProcess;
    @Override
    public void process(DefaultGame game) {
        game.getGameState().startAffectingCardsForCurrentPlayer(game);

        SystemQueueAction action = new SystemQueueAction();

        action.appendEffect(new UnrespondableEffect() {
            @Override
            protected void doPlayEffect() {
                var state = game.getGameState();
                state.sendMessage("\n\n========\n\nStart of " + state.getCurrentPlayerId() + "'s turn.");
            }
        });

        action.appendEffect(
                new TriggeringResultEffect(game, new StartOfTurnResult(), "Start of turn"));
        ((ModifiersLogic) game.getModifiersEnvironment()).signalStartOfTurn(game.getGameState().getCurrentPlayerId());
        game.getActionsEnvironment().addActionToStack(action);
        if (Objects.equals(game.getFormat().getGameType(), "tribbles"))
            _followingGameProcess = new TribblesTurnProcess();
        else if (Objects.equals(game.getFormat().getGameType(), "st1e"))
            _followingGameProcess = new ST1ETurnProcess();
    }

    @Override
    public GameProcess getNextProcess() { return _followingGameProcess; }
}
