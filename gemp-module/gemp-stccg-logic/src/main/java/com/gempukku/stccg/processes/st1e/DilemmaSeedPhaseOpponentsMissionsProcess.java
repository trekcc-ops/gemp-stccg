package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.processes.GameProcess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        for (MissionLocation location: _game.getGameState().getSpacelineLocations()) {
            MissionCard mission = location.getMissions().getFirst();
            if (location.getMissions().size() == 1 && mission.getOwner() != player)
                result.add(mission);
        }
        return result;
    }

    @Override
    protected String getDecisionText(Player player) {
        String opponentId = player.getGame().getOpponent(player.getPlayerId());
        return "Select a mission of " + opponentId + "'s to seed cards under or remove cards from";
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        if (_playersParticipating.isEmpty()) {
            for (MissionLocation location : _game.getGameState().getSpacelineLocations()) {
                location.seedPreSeeds();
            }
            return new DilemmaSeedPhaseYourMissionsProcess(_game);
        }
        else return new DilemmaSeedPhaseOpponentsMissionsProcess(_playersParticipating, _game);
    }
}