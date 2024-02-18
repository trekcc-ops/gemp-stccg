package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.SystemQueueAction;
import com.gempukku.stccg.effects.TriggeringResultEffect;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.results.StartOfTurnResult;

public class TribblesStartOfTurnGameProcess extends GameProcess {
    private GameProcess _followingGameProcess;
    private TribblesGame _game;
    public TribblesStartOfTurnGameProcess(TribblesGame game) {
        _game = game;
    }
    @Override
    public void process() {
        _game.getGameState().startAffectingCardsForCurrentPlayer();

        SystemQueueAction action = new SystemQueueAction();

        action.appendEffect(new UnrespondableEffect() {
            @Override
            protected void doPlayEffect() {
                var state = _game.getGameState();
                state.sendMessage("\n\n========\n\nStart of " + state.getCurrentPlayerId() + "'s turn.");
            }
        });

        action.appendEffect(
                new TriggeringResultEffect(_game, new StartOfTurnResult(), "Start of turn"));
        ((ModifiersLogic) _game.getModifiersEnvironment()).signalStartOfTurn(_game.getGameState().getCurrentPlayerId());
        _game.getActionsEnvironment().addActionToStack(action);
        _followingGameProcess = new TribblesTurnProcess(_game);
        // TODO - Remove commented code below
        // DEBUG - End game to see if this is working
//        game.playerWon(game.getGameState().getCurrentPlayerId(), "testing");
    }

    @Override
    public GameProcess getNextProcess() { return _followingGameProcess; }
}
