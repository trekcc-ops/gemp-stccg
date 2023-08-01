package com.gempukku.lotro.processes.lotronly;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.processes.turn.EndOfPhaseGameProcess;
import com.gempukku.lotro.processes.turn.PlayerPlaysPhaseActionsUntilPassesGameProcess;
import com.gempukku.lotro.processes.turn.StartOfPhaseGameProcess;
import com.gempukku.lotro.processes.GameProcess;

public class FellowshipGameProcess implements GameProcess {
    private GameProcess _followingGameProcess;

    @Override
    public void process(DefaultGame game) {
        if (game.getModifiersQuerying().shouldSkipPhase(game, Phase.FELLOWSHIP, game.getGameState().getCurrentPlayerId()))
            _followingGameProcess = new ShadowPhasesGameProcess();
        else
            _followingGameProcess = new StartOfPhaseGameProcess(Phase.FELLOWSHIP,
                    new PlayerPlaysPhaseActionsUntilPassesGameProcess(game.getGameState().getCurrentPlayerId(),
                            new MovementGameProcess(
                                    new EndOfPhaseGameProcess(Phase.FELLOWSHIP,
                                            game.getFormat().getAdventure().getAfterFellowshipPhaseGameProcess()))));
    }

    @Override
    public GameProcess getNextProcess() {
        return _followingGameProcess;
    }
}