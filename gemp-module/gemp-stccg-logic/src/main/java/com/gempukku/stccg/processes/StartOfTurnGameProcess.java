package com.gempukku.stccg.processes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.actions.turn.StartTurnAction;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.processes.st1e.ST1EPlayPhaseSegmentProcess;
import com.gempukku.stccg.processes.tribbles.TribblesPlayerPlaysOrDraws;

@JsonTypeName("StartOfTurnGameProcess")
public class StartOfTurnGameProcess extends GameProcess {
    @Override
    public void process(DefaultGame cardGame) throws InvalidGameLogicException {
        cardGame.sendMessage("\n\n========\n\nStart of " + cardGame.getCurrentPlayerId() + "'s turn.");
        cardGame.getGameState().setCurrentPhase(Phase.START_OF_TURN);
        cardGame.sendActionResultToClient(); // for phase and turn change
        cardGame.getActionsEnvironment().addActionToStack(new StartTurnAction(cardGame, cardGame.getCurrentPlayerId()));
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        if (cardGame instanceof TribblesGame tribblesGame) {
            return new TribblesPlayerPlaysOrDraws(tribblesGame);
        } else if (cardGame instanceof ST1EGame) {
            cardGame.getGameState().setCurrentPhase(Phase.CARD_PLAY);
            cardGame.sendActionResultToClient(); // for phase change
            return new ST1EPlayPhaseSegmentProcess(cardGame.getCurrentPlayerId());
        }
        throw new InvalidGameLogicException("No start of turn process defined for game type");
    }

}