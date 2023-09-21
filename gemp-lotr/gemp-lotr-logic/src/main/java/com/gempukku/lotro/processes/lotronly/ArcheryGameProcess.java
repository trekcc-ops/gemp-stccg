package com.gempukku.lotro.processes.lotronly;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.processes.GameProcess;
import com.gempukku.lotro.processes.EndOfPhaseGameProcess;
import com.gempukku.lotro.processes.PlayersPlayPhaseActionsInOrderGameProcess;
import com.gempukku.lotro.processes.StartOfPhaseGameProcess;

public class ArcheryGameProcess implements GameProcess {
    private GameProcess _followingGameProcess;

    @Override
    public void process(DefaultGame game) {
        if (Filters.countActive(game, CardType.MINION)==0
                || game.getModifiersQuerying().shouldSkipPhase(game, Phase.ARCHERY, null))
            _followingGameProcess = new AssignmentGameProcess();
        else
            _followingGameProcess = new StartOfPhaseGameProcess(Phase.ARCHERY,
                    new PlayersPlayPhaseActionsInOrderGameProcess(game.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(game.getGameState().getCurrentPlayerId(), true), 0,
                // done messed this up just so we could delete some stuff
//                            new ArcheryFireGameProcess(
                                    new EndOfPhaseGameProcess(Phase.ARCHERY,
                                            new AssignmentGameProcess())));
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}
