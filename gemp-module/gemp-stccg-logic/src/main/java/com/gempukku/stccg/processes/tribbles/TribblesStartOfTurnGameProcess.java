package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.turn.AllowResponsesAction;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.game.DefaultGame;
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

        SystemQueueAction action = new SystemQueueAction(_game) {
            @Override
            public Action nextAction(DefaultGame cardGame) {
                cardGame.sendMessage("\n\n========\n\nStart of " + cardGame.getCurrentPlayerId() + "'s turn.");
                return new AllowResponsesAction(_game, EffectResult.Type.START_OF_TURN);
            }
        };

        ((ModifiersLogic) _game.getModifiersEnvironment()).signalStartOfTurn(_game.getGameState().getCurrentPlayerId());
        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        return new TribblesPlayerPlaysOrDraws(_game);
    }

}