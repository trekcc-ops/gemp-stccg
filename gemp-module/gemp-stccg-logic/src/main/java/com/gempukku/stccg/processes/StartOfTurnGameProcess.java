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
        cardGame.getActionsEnvironment().addActionToStack(new StartTurnAction(cardGame));
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        if (cardGame instanceof TribblesGame tribblesGame) {
            return new TribblesPlayerPlaysOrDraws(tribblesGame);
        } else if (cardGame instanceof ST1EGame firstEditionGame) {
            firstEditionGame.setCurrentPhase(Phase.CARD_PLAY);
            String message = "Start of " + Phase.CARD_PLAY + " phase";
            firstEditionGame.sendMessage("\n" + message);
            return new ST1EPlayPhaseSegmentProcess();
        }
        throw new InvalidGameLogicException("No start of turn process defined for game type");
    }

}