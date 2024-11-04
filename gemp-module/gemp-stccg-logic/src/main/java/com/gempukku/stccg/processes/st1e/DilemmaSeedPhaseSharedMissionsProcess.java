package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.processes.GameProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DilemmaSeedPhaseSharedMissionsProcess extends DilemmaSeedPhaseProcess {
    DilemmaSeedPhaseSharedMissionsProcess(Set<String> playersDone, ST1EGame game) {
        super(playersDone, game);
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
        Set<String> players = _game.getPlayerIds();
        if (players.size() == _playersDone.size()) {
            for (ST1ELocation location : _game.getGameState().getSpacelineLocations()) {
                location.getMissions().getFirst().seedPreSeeds();
            }
            _game.getActionsEnvironment().signalEndOfPhase();
            _game.getGameState().setCurrentPhase(Phase.SEED_FACILITY);
            _game.takeSnapshot("Start of facility seed phase");
            return new ST1EFacilitySeedPhaseProcess(0, _game); // TODO - Add "other cards" dilemma seed phase
        }
        else return new DilemmaSeedPhaseSharedMissionsProcess(_playersDone, _game);
    }
}