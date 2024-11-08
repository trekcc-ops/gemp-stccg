package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.processes.GameProcess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DilemmaSeedPhaseSharedMissionsProcess extends DilemmaSeedPhaseProcess {
    DilemmaSeedPhaseSharedMissionsProcess(ST1EGame game) {
        super(game.getPlayerIds(), game);
    }
    DilemmaSeedPhaseSharedMissionsProcess(Collection<String> playersSelecting, ST1EGame game) {
        super(playersSelecting, game);
    }


    @Override
    List<MissionCard> getAvailableMissions(Player player) {
        List<MissionCard> result = new ArrayList<>();
        for (ST1ELocation location: _game.getGameState().getSpacelineLocations()) {
            MissionCard mission = location.getMissions().getFirst();
            if (location.getMissions().size() == 2)
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
            _game.getGameState().setCurrentPhase(Phase.SEED_FACILITY);
            _game.takeSnapshot("Start of facility seed phase");
            return new ST1EFacilitySeedPhaseProcess(0, _game); // TODO - Add "other cards" dilemma seed phase
        }
        else return new DilemmaSeedPhaseSharedMissionsProcess(_playersParticipating, _game);
    }
}