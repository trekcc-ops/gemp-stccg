package com.gempukku.stccg.condition;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class HigherScoreThanAllOtherPlayersCondition implements Condition {
    private final Player _highestScorePlayer;

    public HigherScoreThanAllOtherPlayersCondition(Player highestScorePlayer) {
        _highestScorePlayer = highestScorePlayer;
    }

    @Override
    public boolean isFulfilled() {
        DefaultGame cardGame = _highestScorePlayer.getGame();
        for (Player player : cardGame.getPlayers()) {
            if (player.getScore() >= _highestScorePlayer.getScore() && _highestScorePlayer != player) {
                return false;
            }
        }
        return true;
    }
}