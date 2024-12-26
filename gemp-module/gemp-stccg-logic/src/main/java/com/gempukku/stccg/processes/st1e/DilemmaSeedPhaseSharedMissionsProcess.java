package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.processes.GameProcess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DilemmaSeedPhaseSharedMissionsProcess extends DilemmaSeedPhaseProcess {
    DilemmaSeedPhaseSharedMissionsProcess(ST1EGame game) {
        super(game.getPlayerIds(), game);
    }
    public DilemmaSeedPhaseSharedMissionsProcess(Collection<String> playersSelecting, ST1EGame game) {
        super(playersSelecting, game);
    }


    @Override
    List<MissionCard> getAvailableMissions(Player player) {
        List<MissionCard> result = new ArrayList<>();
        for (MissionLocation location: _game.getGameState().getSpacelineLocations()) {
            MissionCard mission = location.getMissions().getFirst();
            if (location.getMissions().size() == 2)
                result.add(mission);
        }
        return result;
    }

    @Override
    protected String getDecisionText(Player player) {
        return "Select a shared mission to seed cards under or remove cards from";
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        if (_playersParticipating.isEmpty()) {
            for (MissionLocation location : _game.getGameState().getSpacelineLocations()) {
                location.seedPreSeeds();
            }
            _game.getGameState().setCurrentPhase(Phase.SEED_FACILITY);
            _game.takeSnapshot("Start of facility seed phase");
            return new ST1EFacilitySeedPhaseProcess(0, _game); // TODO - Add "other cards" dilemma seed phase
        }
        else return new DilemmaSeedPhaseSharedMissionsProcess(_playersParticipating, _game);
    }
}