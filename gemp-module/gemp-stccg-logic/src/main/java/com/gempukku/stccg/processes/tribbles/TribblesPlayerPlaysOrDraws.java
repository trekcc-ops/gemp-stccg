package com.gempukku.stccg.processes.tribbles;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;

public class TribblesPlayerPlaysOrDraws extends TribblesGameProcess {

    public TribblesPlayerPlaysOrDraws(TribblesGame game) {
        super(game);
    }

    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {
        Player currentPlayer = _game.getCurrentPlayer();
        final List<TopLevelSelectableAction> playableActions =
                _game.getActionsEnvironment().getPhaseActions(_game, currentPlayer);

        if (playableActions.isEmpty() && _game.shouldAutoPass(_game.getGameState().getCurrentPhase())) {
            _consecutivePasses++;
        } else {
            TribblesGame thisGame = _game; // to avoid conflicts when decision calls "_game"
            cardGame.sendAwaitingDecision(
                    new ActionSelectionDecision(currentPlayer, DecisionContext.SELECT_TRIBBLES_ACTION, playableActions, cardGame, false) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            try {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    thisGame.getActionsEnvironment().addActionToStack(action);
                                } else
                                    _consecutivePasses++;
                            } catch(InvalidGameLogicException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        return (_consecutivePasses > 0) ? new TribblesPlayerDrawsAndCanPlayProcess(_game) :
                new TribblesEndOfTurnGameProcess(_game);
    }
}