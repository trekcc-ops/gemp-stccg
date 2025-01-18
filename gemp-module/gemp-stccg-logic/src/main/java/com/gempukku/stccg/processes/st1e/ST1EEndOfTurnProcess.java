package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

import java.util.List;

public class ST1EEndOfTurnProcess extends ST1EGameProcess {

    public ST1EEndOfTurnProcess(ST1EGame game) {
        super(game);
    }

    @Override
    public void process(DefaultGame cardGame) {
        DefaultGame thisGame = _game;
        String playerId = _game.getCurrentPlayerId();
        for (PhysicalCard card : Filters.filterActive(_game, Filters.ship))
            ((PhysicalShipCard) card).restoreRange();
        _game.getGameState().playerDrawsCard(playerId);
        _game.sendMessage(playerId + " drew their normal end-of-turn card draw");
        final List<TopLevelSelectableAction> playableActions = _game.getActionsEnvironment().getPhaseActions(playerId);
        Phase phase = thisGame.getCurrentPhase();
        if (!playableActions.isEmpty() || !_game.shouldAutoPass(phase)) {
            thisGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(thisGame.getCurrentPlayer(),
                            "Play " + phase + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    thisGame.getActionsEnvironment().addActionToStack(action);
                                } else {
                                    _consecutivePasses++;
                                }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        _game.getModifiersEnvironment().signalEndOfTurn(); // Remove "until end of turn" modifiers
        _game.getActionsEnvironment().signalEndOfTurn(); // Remove "until end of turn" permitted actions
        _game.getGameState().sendMessage(_game.getCurrentPlayerId() + " ended their turn");
        _game.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
        String playerId = _game.getGameState().getCurrentPlayerId();
        ActionOrder actionOrder = _game.getGameState().getPlayerOrder().getClockwisePlayOrder(playerId, false);
        actionOrder.getNextPlayer();

        String nextPlayer = actionOrder.getNextPlayer();
        _game.getGameState().startPlayerTurn(nextPlayer);
        return new StartOfTurnGameProcess();
    }
}