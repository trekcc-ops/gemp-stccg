package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.processes.GameProcess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DilemmaSeedPhaseYourMissionsProcess extends DilemmaSeedPhaseProcess {

    DilemmaSeedPhaseYourMissionsProcess(ST1EGame game) {
        super(game.getPlayerIds(), game);
    }
    DilemmaSeedPhaseYourMissionsProcess(Collection<String> playersSelecting, ST1EGame game) {
        super(playersSelecting, game);
    }

    @Override
    List<MissionCard> getAvailableMissions(Player player) {
        List<MissionCard> result = new ArrayList<>();
        for (ST1ELocation location: _game.getGameState().getSpacelineLocations()) {
            MissionCard mission = location.getMissions().getFirst();
            if (location.getMissions().size() == 1 && mission.getOwner() == player)
                result.add(mission);
        }
        return result;
    }

    @Override
    public GameProcess getNextProcess() {
        if (_playersParticipating.isEmpty()) {
            for (ST1ELocation location : _game.getGameState().getSpacelineLocations()) {
                location.getMissions().getFirst().seedPreSeeds();
            }
            return new DilemmaSeedPhaseSharedMissionsProcess(_game);
        }
        else return new DilemmaSeedPhaseYourMissionsProcess(_playersParticipating, _game);
    }
}