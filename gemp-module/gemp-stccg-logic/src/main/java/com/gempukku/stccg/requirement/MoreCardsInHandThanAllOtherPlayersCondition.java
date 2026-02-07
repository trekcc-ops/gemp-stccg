package com.gempukku.stccg.requirement;

import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.YouPlayerResolver;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public class MoreCardsInHandThanAllOtherPlayersCondition implements Condition {
    private final YouPlayerResolver _playerResolver;

    public MoreCardsInHandThanAllOtherPlayersCondition(YouPlayerResolver playerResolver) {
        _playerResolver = playerResolver;
    }


    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        try {
            Player targetPlayer = cardGame.getPlayer(_playerResolver.getPlayerName());
            for (Player player : cardGame.getPlayers()) {
                if (player.getCardsInHand().size() >= targetPlayer.getCardsInHand().size() && targetPlayer != player) {
                    return false;
                }
            }
            return true;
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }
}