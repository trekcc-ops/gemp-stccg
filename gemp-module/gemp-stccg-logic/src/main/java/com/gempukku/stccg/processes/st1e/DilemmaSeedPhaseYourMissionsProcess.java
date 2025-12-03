package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.processes.GameProcess;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonTypeName("DilemmaSeedPhaseYourMissionsProcess")
public class DilemmaSeedPhaseYourMissionsProcess extends DilemmaSeedPhaseProcess {

    DilemmaSeedPhaseYourMissionsProcess(ST1EGame game) {
        super(game.getPlayerIds());
    }
    @ConstructorProperties({"playersParticipating"})
    public DilemmaSeedPhaseYourMissionsProcess(Collection<String> playersSelecting) {
        super(playersSelecting);
    }

    @Override
    List<MissionLocation> getAvailableMissions(ST1EGame stGame, String playerId) {
        List<MissionLocation> result = new ArrayList<>();
        for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
            MissionCard mission = location.getMissionCards().getFirst();
            if (location.getMissionCards().size() == 1 && mission.isOwnedBy(playerId))
                result.add(location);
        }
        return result;
    }


    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        ST1EGame stGame = getST1EGame(cardGame);
        if (_playersParticipating.isEmpty()) {
            for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
                location.seedPreSeedsForYourMissions(cardGame);
            }
            cardGame.setCurrentPhase(Phase.SEED_FACILITY);
            return new ST1EFacilitySeedPhaseProcess(0);
        }
        else return new DilemmaSeedPhaseYourMissionsProcess(_playersParticipating);

    }
}