package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST2EGame;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ST2EGameState extends GameState {
    private final Map<String, List<PhysicalCard>> _tableCards;
    private final ST2EGame _game;

    public ST2EGameState(Iterable<String> playerIds, ST2EGame game) {
        super(game, playerIds);
        _game = game;
        _tableCards = new HashMap<>();
        for (String player : playerIds)
            _tableCards.put(player, new LinkedList<>());
        _currentPhase = Phase.SEED_DOORWAY;
    }

    @Override
    public ST2EGame getGame() { return _game; }

    @Override
    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        Player player = getPlayer(playerId);
        if (zone == Zone.DRAW_DECK || zone == Zone.HAND || zone == Zone.DISCARD || zone == Zone.REMOVED || zone == Zone.VOID)
            return player.getCardGroup(zone);
        else if (zone == Zone.TABLE)
            return _tableCards.get(playerId);
        else // This should never be accessed
            return _inPlay;
    }

}