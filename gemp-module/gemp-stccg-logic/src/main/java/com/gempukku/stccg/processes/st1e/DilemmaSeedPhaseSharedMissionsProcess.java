package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
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
    List<MissionCard> getAvailableMissions(ST1EGame stGame, String playerId) {
        List<MissionCard> result = new ArrayList<>();
        for (MissionLocation location: stGame.getGameState().getSpacelineLocations()) {
            MissionCard mission = location.getMissionCards().getFirst();
            if (location.getMissionCards().size() == 2)
                result.add(mission);
        }
        return result;
    }

    @Override
    protected String getDecisionText(DefaultGame cardGame, Player player) {
        return "Select a shared mission to seed cards under or remove cards from";
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        ST1EGame stGame = getST1EGame(cardGame);
        if (_playersParticipating.isEmpty()) {
            for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
                location.seedPreSeedsForSharedMissions(stGame);
            }
            return new DilemmaSeedPhaseYourMissionsProcess(stGame);
        }
        else return new DilemmaSeedPhaseSharedMissionsProcess(_playersParticipating);

    }
}