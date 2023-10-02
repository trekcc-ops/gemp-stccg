package com.gempukku.lotro.processes;

import com.gempukku.lotro.game.ST1EGame;

public class ST1EDoorwaySeedPhaseProcess implements GameProcess<ST1EGame> {
    private String _firstPlayer;

    public ST1EDoorwaySeedPhaseProcess(String playerId) {
        _firstPlayer = playerId;
        // TODO: Build code for the doorway phase
    }

    @Override
    public void process(ST1EGame game) {
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() { return new ST1EMissionSeedPhaseProcess(_firstPlayer);
    }
}