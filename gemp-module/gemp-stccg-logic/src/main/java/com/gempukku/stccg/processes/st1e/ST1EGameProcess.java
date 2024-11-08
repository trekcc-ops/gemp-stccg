package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@JsonSerialize(using = ST1EProcessSerializer.class)
public abstract class ST1EGameProcess extends GameProcess {
    protected final ST1EGame _game;
    final Set<String> _playersParticipating;

    public ST1EGameProcess(Player player,  ST1EGame game) {
        _game = game;
        _playersParticipating = new HashSet<>();
        _playersParticipating.add(player.getPlayerId());
    }


    public ST1EGameProcess(Collection<String> playersSelecting, ST1EGame game) {
        _playersParticipating = new HashSet<>(playersSelecting);
        _game = game;
    }

}