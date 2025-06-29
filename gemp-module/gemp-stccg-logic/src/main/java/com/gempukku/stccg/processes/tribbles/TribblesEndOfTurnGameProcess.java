package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.turn.EndTurnAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.processes.GameProcess;

public class TribblesEndOfTurnGameProcess extends TribblesGameProcess {
    public TribblesEndOfTurnGameProcess(TribblesGame game) {
        super(game);
    }
    @Override
    public void process(DefaultGame cardGame) throws InvalidGameLogicException {
        try {
            Player currentPlayer = _game.getCurrentPlayer();
            Action action = new EndTurnAction(_game, currentPlayer);
            _game.getActionsEnvironment().addActionToStack(action);
        } catch(PlayerNotFoundException exp) {
            throw new InvalidGameLogicException(exp.getMessage());
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        if (_game.getGameState().isCurrentRoundOver())
            return new TribblesEndOfRoundGameProcess(_game);
        else return new TribblesBetweenTurnsProcess(_game);
    }
}