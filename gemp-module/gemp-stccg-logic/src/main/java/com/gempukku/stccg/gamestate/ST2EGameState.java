package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.ST2EGame;
import com.gempukku.stccg.player.PlayerClock;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ST2EGameState extends GameState {

    public ST2EGameState(Iterable<String> playerIds, ST2EGame game, Map<String, PlayerClock> clocks) {
        super(game, playerIds, clocks);
        _currentPhase = Phase.SEED_DOORWAY;
    }

    public void checkVictoryConditions(DefaultGame cardGame) {
        // TODO - VERY simplistic. Just a straight race to 100.
        // TODO - Does not account for possible scenario where both players go over 100 simultaneously
        for (Player player : getPlayers()) {
            int score = player.getScore();
            if (score >= 100)
                cardGame.playerWon(player.getPlayerId(), score + " points");
        }
    }

    @Override
    public List<Phase> getPhasesInOrder() {
        return List.of(getCurrentPhase());
    }

}