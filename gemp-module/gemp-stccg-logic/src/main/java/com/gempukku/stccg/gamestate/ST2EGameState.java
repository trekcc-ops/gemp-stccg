package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.ST2EGame;

import java.util.*;

public class ST2EGameState extends GameState {
    private final Map<String, List<PhysicalCard>> _tableCards;
    private final ST2EGame _game;

    public ST2EGameState(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library, GameFormat format, ST2EGame game) {
        super(decks, library, format);
        _game = game;
        _tableCards = new HashMap<>();
        for (String player : players) {
            _tableCards.put(player, new LinkedList<>());
        }
        _currentPhase = Phase.SEED_DOORWAY;
    }

    @Override
    public ST2EGame getGame() { return _game; }

    @Override
    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DRAW_DECK || zone == Zone.HAND || zone == Zone.DISCARD || zone == Zone.REMOVED)
            return _cardGroups.get(zone).get(playerId);
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
        else if (zone == Zone.TABLE)
            return _tableCards.get(playerId);
        else // This should never be accessed
            return _inPlay;
    }

}