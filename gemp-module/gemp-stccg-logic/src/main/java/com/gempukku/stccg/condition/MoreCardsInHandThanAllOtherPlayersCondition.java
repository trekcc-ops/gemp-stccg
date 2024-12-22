package com.gempukku.stccg.condition;

import com.gempukku.stccg.cards.blueprints.resolver.YouPlayerResolver;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

public class MoreCardsInHandThanAllOtherPlayersCondition implements Condition {
    private final YouPlayerResolver _playerResolver;

    public MoreCardsInHandThanAllOtherPlayersCondition(YouPlayerResolver playerResolver) {
        _playerResolver = playerResolver;
    }


    @Override
    public boolean isFulfilled(DefaultGame cardGame) {
        Player targetPlayer = _playerResolver.getPlayer();
        for (Player player : cardGame.getPlayers()) {
            if (player.getHand().size() >= targetPlayer.getHand().size() && targetPlayer != player) {
                return false;
            }
        }
        return true;
    }
}