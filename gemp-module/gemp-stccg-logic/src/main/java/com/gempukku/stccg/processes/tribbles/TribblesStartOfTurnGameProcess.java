package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.turn.StartOfTurnResult;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.actions.turn.TriggeringResultEffect;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.processes.GameProcess;

public class TribblesStartOfTurnGameProcess extends TribblesGameProcess {
    public TribblesStartOfTurnGameProcess(TribblesGame game) {
        super(game);
    }
    @Override
    public void process() {
        // TODO - Don't fully understand this commented method, but it creates duplicates of modifiers
//        _game.getGameState().startAffectingCardsForCurrentPlayer();

        SystemQueueAction action = new SystemQueueAction(_game);

        action.appendEffect(new UnrespondableEffect(_game) {
            @Override
            protected void doPlayEffect() {
                var state = _game.getGameState();
                state.sendMessage("\n\n========\n\nStart of " + state.getCurrentPlayerId() + "'s turn.");
            }
        });

        action.appendEffect(
                new TriggeringResultEffect(_game, new StartOfTurnResult(_game), "Start of turn"));
        ((ModifiersLogic) _game.getModifiersEnvironment()).signalStartOfTurn(_game.getGameState().getCurrentPlayerId());
        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesPlayerPlaysOrDraws(_game);
    }

}