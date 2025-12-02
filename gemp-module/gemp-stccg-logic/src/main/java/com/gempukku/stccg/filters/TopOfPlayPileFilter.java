package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;

public class TopOfPlayPileFilter implements CardFilter {

    @JsonProperty("playerName")
    private final String _playerName;

    public TopOfPlayPileFilter(Player player) {
        _playerName = player.getPlayerId();
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        if (game instanceof TribblesGame tribblesGame) {
            return tribblesGame.getGameState().getPlayPile(_playerName).getLast() == physicalCard;
        } else {
            return false;
        }
    }
}