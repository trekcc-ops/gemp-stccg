package com.gempukku.lotro.processes;

import com.gempukku.lotro.game.DefaultGame;

public class ST1EDoorwaySeedPhaseProcess implements GameProcess {
    private String _firstPlayer;

    public ST1EDoorwaySeedPhaseProcess(String playerId) {
        _firstPlayer = playerId;
    }

    @Override
    public void process(DefaultGame game) {
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EDoorwaySeedPhaseProcess(_firstPlayer);
    }
}