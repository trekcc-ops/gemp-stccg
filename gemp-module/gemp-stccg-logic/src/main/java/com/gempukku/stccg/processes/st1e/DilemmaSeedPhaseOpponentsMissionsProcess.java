package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

public class DilemmaSeedPhaseOpponentsMissionsProcess extends DilemmaSeedPhaseProcess {

    DilemmaSeedPhaseOpponentsMissionsProcess(ST1EGame game) {
        super(game.getPlayerIds(), game);
    }

    public DilemmaSeedPhaseOpponentsMissionsProcess(Collection<String> playersSelecting, ST1EGame game) {
        super(playersSelecting, game);
    }

    @Override
    List<MissionCard> getAvailableMissions(Player player) {
        List<MissionCard> result = new ArrayList<>();
        for (ST1ELocation location: _game.getGameState().getSpacelineLocations()) {
            MissionCard mission = location.getMissions().getFirst();
            if (location.getMissions().size() == 1 && mission.getOwner() != player)
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
            return new DilemmaSeedPhaseYourMissionsProcess(_game);
        }
        else return new DilemmaSeedPhaseOpponentsMissionsProcess(_playersParticipating, _game);
    }
}