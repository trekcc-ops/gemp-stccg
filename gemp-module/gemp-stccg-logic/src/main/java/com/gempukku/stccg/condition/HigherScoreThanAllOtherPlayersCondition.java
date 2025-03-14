package com.gempukku.stccg.condition;

import com.gempukku.stccg.player.YouPlayerResolver;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public class HigherScoreThanAllOtherPlayersCondition implements Condition {
    private final YouPlayerResolver _playerResolver;

    public HigherScoreThanAllOtherPlayersCondition(YouPlayerResolver playerResolver) {
        _playerResolver = playerResolver;
    }

    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        Player targetPlayer = _playerResolver.getPlayer();
        for (Player player : cardGame.getPlayers()) {
            if (player.getScore() >= targetPlayer.getScore() && targetPlayer != player) {
                return false;
            }
        }
        return true;
    }
}