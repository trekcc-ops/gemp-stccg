package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.turn.AllowResponsesAction;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

public class ST1EStartOfTurnGameProcess extends ST1EGameProcess {
    public ST1EStartOfTurnGameProcess(ST1EGame game) {
        super(game);
    }
    @Override
    public void process(DefaultGame cardGame) {

        SystemQueueAction action = new SystemQueueAction(_game);

        action.appendAction(new SystemQueueAction(_game) {
            @Override
            public Action nextAction(DefaultGame cardGame) {
                _game.sendMessage("\n\n========\n\nStart of " + _game.getCurrentPlayerId() + "'s turn.");
                return getNextAction();
            }
        });

        action.appendAction(new AllowResponsesAction(_game, EffectResult.Type.START_OF_TURN));
        _game.getModifiersEnvironment().signalStartOfTurn();
        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        _game.getGameState().setCurrentPhase(Phase.CARD_PLAY);
        String message = "Start of " + Phase.CARD_PLAY + " phase";
        _game.sendMessage("\n" + message);
        return new ST1EPlayPhaseSegmentProcess(_game);
    }
}