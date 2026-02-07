package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.processes.GameProcess;

import java.beans.ConstructorProperties;
import java.util.*;

@JsonTypeName("DilemmaSeedPhaseOpponentsMissionsProcess")
public class DilemmaSeedPhaseOpponentsMissionsProcess extends DilemmaSeedPhaseProcess {

    DilemmaSeedPhaseOpponentsMissionsProcess(ST1EGame game) {
        super(game.getPlayerIds());
    }

    @ConstructorProperties({"playersParticipating"})
    public DilemmaSeedPhaseOpponentsMissionsProcess(Collection<String> playersSelecting) {
        super(playersSelecting);
    }

    @Override
    List<MissionLocation> getAvailableMissions(ST1EGame stGame, String playerId) {
        List<MissionLocation> result = new ArrayList<>();
        for (MissionLocation location : stGame.getGameState().getUnorderedMissionLocations()) {
            MissionCard mission = location.getMissionCards().getFirst();
            if (location.getMissionCards().size() == 1 && !Objects.equals(mission.getOwnerName(), playerId))
                result.add(location);
        }
        return result;
    }


    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        ST1EGame stGame = getST1EGame(cardGame);
        if (_playersParticipating.isEmpty()) {
            for (MissionLocation location : stGame.getGameState().getUnorderedMissionLocations()) {
                location.seedPreSeedsForOpponentsMissions(cardGame);
            }
            return new DilemmaSeedPhaseSharedMissionsProcess(stGame);
        }
        else return new DilemmaSeedPhaseOpponentsMissionsProcess(_playersParticipating);
    }
}