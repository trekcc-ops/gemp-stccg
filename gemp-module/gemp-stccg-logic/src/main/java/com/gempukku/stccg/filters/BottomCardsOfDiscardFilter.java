package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.Collection;
import java.util.List;

public class BottomCardsOfDiscardFilter implements CardFilter {

    private final Player _player;
    private final Collection<Filterable> _filterables;
    private final int _cardCount;

    public BottomCardsOfDiscardFilter(Player player, int cardCount, Filterable... filterables) {
        _player = player;
        _filterables = List.of(filterables);
        _cardCount = cardCount;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        List<PhysicalCard> discardCards = _player.getCardGroupCards(Zone.DISCARD);
        int cardsIdentified = 0;
        for (int i = discardCards.size() - 1; i >= 0; i--) {
            if (Filters.and(_filterables).accepts(game, discardCards.get(i)) && cardsIdentified < _cardCount) {
                if (physicalCard == discardCards.get(i)) {
                    return true;
                }
                cardsIdentified++;
            }
        }
        return false;
    }
}