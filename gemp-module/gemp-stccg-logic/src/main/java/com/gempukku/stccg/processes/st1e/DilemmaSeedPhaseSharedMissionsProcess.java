package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.processes.GameProcess;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonTypeName("DilemmaSeedPhaseSharedMissionsProcess")
public class DilemmaSeedPhaseSharedMissionsProcess extends DilemmaSeedPhaseProcess {
    DilemmaSeedPhaseSharedMissionsProcess(ST1EGame game) {
        super(game.getPlayerIds());
    }

    @ConstructorProperties({"playersParticipating"})
    public DilemmaSeedPhaseSharedMissionsProcess(Collection<String> playersSelecting) {
        super(playersSelecting);
    }


    @Override
    List<MissionLocation> getAvailableMissions(ST1EGame stGame, String playerId) {
        List<MissionLocation> result = new ArrayList<>();
        for (MissionLocation location: stGame.getGameState().getUnorderedMissionLocations()) {
            if (location.isSharedMission())
                result.add(location);
        }
        return result;
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        ST1EGame stGame = getST1EGame(cardGame);
        if (_playersParticipating.isEmpty()) {
            for (MissionLocation location : stGame.getGameState().getUnorderedMissionLocations()) {
                location.seedPreSeedsForSharedMissions(stGame);
            }
            return new DilemmaSeedPhaseYourMissionsProcess(stGame);
        }
        else return new DilemmaSeedPhaseSharedMissionsProcess(_playersParticipating);

    }
}