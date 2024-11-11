package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.game.ST1EGame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class SimultaneousGameProcess extends ST1EGameProcess {

    protected final List<String> _playersParticipating;

    SimultaneousGameProcess(Collection<String> playersParticipating, ST1EGame game) {
        super(game);
        _playersParticipating = new LinkedList<>(playersParticipating);
    }

    public List<String> getPlayersParticipating() { return _playersParticipating; }

}