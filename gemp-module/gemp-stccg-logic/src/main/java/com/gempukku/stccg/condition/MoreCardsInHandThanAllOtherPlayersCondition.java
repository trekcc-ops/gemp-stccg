package com.gempukku.stccg.condition;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class MoreCardsInHandThanAllOtherPlayersCondition implements Condition {
    private final Player _mostCardsPlayer;

    public MoreCardsInHandThanAllOtherPlayersCondition(Player mostCardsPlayer) {
        _mostCardsPlayer = mostCardsPlayer;
    }

    @Override
    public boolean isFulfilled() {
        DefaultGame cardGame = _mostCardsPlayer.getGame();
        for (Player player : cardGame.getPlayers()) {
            if (player.getHand().size() >= _mostCardsPlayer.getHand().size() && _mostCardsPlayer != player) {
                return false;
            }
        }
        return true;
    }
}