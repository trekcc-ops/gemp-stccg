package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.turn.StartOfTurnResult;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.actions.turn.TriggeringResultEffect;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

public class ST1EStartOfTurnGameProcess extends ST1EGameProcess {
    public ST1EStartOfTurnGameProcess(ST1EGame game) {
        super(game);
    }
    @Override
    public void process() {

        SystemQueueAction action = new SystemQueueAction(_game);

        action.appendAction(new SystemQueueAction(_game) {
            @Override
            public Action nextAction(DefaultGame cardGame) {
                _game.sendMessage("\n\n========\n\nStart of " + _game.getCurrentPlayerId() + "'s turn.");
                return getNextAction();
            }
        });

        action.appendAction(new SubAction(action,
                new TriggeringResultEffect(_game, new StartOfTurnResult(_game), "Start of turn")));
        _game.getModifiersEnvironment().signalStartOfTurn();
        _game.getActionsEnvironment().addActionToStack(action);
    }

    @Override
    public GameProcess getNextProcess() {
        _game.getGameState().setCurrentPhase(Phase.CARD_PLAY);
        String message = "Start of " + Phase.CARD_PLAY + " phase";
        _game.sendMessage("\n" + message);
        return new ST1EPlayPhaseSegmentProcess(_game);
    }
}