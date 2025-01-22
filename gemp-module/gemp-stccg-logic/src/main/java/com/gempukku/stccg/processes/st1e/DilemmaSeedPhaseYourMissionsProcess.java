package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.MissionLocation;
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
    List<MissionCard> getAvailableMissions(ST1EGame stGame, String playerId) {
        List<MissionCard> result = new ArrayList<>();
        try {
            for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
                MissionCard mission = location.getMissions().getFirst();
                if (location.getMissions().size() == 1 && mission.getOwner() == stGame.getPlayer(playerId))
                    result.add(mission);
            }
        } catch(PlayerNotFoundException exp) {
            stGame.sendErrorMessage(exp);
        }
        return result;
    }


    @Override
    protected String getDecisionText(DefaultGame cardGame, Player player) {
        return "Select your mission to seed cards under or remove cards from";
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        ST1EGame stGame = getST1EGame(cardGame);
        if (_playersParticipating.isEmpty()) {
            for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
                location.seedPreSeeds();
            }
            return new DilemmaSeedPhaseSharedMissionsProcess(stGame);
        }
        else return new DilemmaSeedPhaseYourMissionsProcess(_playersParticipating);
    }
}