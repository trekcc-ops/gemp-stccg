package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.processes.GameProcess;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
    List<MissionCard> getAvailableMissions(ST1EGame stGame, String playerId) {
        try {
            Player player = stGame.getPlayer(playerId);
            List<MissionCard> result = new ArrayList<>();
            for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
                MissionCard mission = location.getMissionCards().getFirst();
                if (location.getMissionCards().size() == 1 && mission.getOwner() != player)
                    result.add(mission);
            }
            return result;
        } catch(PlayerNotFoundException exp) {
            stGame.sendErrorMessage(exp);
            return new LinkedList<>();
        }
    }


    @Override
    protected String getDecisionText(DefaultGame cardGame, Player player) {
        String opponentId = cardGame.getOpponent(player.getPlayerId());
        return "Select a mission of " + opponentId + "'s to seed cards under or remove cards from";
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        ST1EGame stGame = getST1EGame(cardGame);
        if (_playersParticipating.isEmpty()) {
            for (MissionLocation location : stGame.getGameState().getSpacelineLocations()) {
                location.seedPreSeedsForOpponentsMissions();
            }
            return new DilemmaSeedPhaseSharedMissionsProcess(stGame);
        }
        else return new DilemmaSeedPhaseOpponentsMissionsProcess(_playersParticipating);
    }
}