package com.gempukku.lotro.game.timing.processes.lotronly;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.timing.processes.GameProcess;
import com.gempukku.lotro.game.timing.processes.lotronly.archery.ArcheryFireGameProcess;
import com.gempukku.lotro.game.timing.processes.turn.EndOfPhaseGameProcess;
import com.gempukku.lotro.game.timing.processes.turn.PlayersPlayPhaseActionsInOrderGameProcess;
import com.gempukku.lotro.game.timing.processes.turn.StartOfPhaseGameProcess;

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
                            new ArcheryFireGameProcess(
                                    new EndOfPhaseGameProcess(Phase.ARCHERY,
                                            new AssignmentGameProcess()))));
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}