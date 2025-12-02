package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class BottomCardsOfDiscardFilter implements CardFilter {

    @JsonProperty("playerName")
    private final String _playerName;

    /* Only pulls bottom cards matching the additional filter. For example, if the additionalFilter specifies
        that cards must be of "personnel" card type, then this filter will return the bottom 3 personnel in the
        discard pile. So if the bottom of the discard pile looks like:
            Ship1 - Personnel1 - Ship2 - Personnel2 - Personnel3
        It will match all 3 personnel, ignoring the ships.
     */
    @JsonProperty("additionalFilter")
    private final CardFilter _additionalFilter;
    @JsonProperty("cardCount")
    private final int _cardCount;

    public BottomCardsOfDiscardFilter(String playerName, int cardCount, CardFilter additionalFilter) {
        _playerName = playerName;
        _additionalFilter = additionalFilter;
        _cardCount = cardCount;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        try {
            Player player = game.getPlayer(_playerName);
            List<PhysicalCard> discardCards = player.getCardGroupCards(Zone.DISCARD);
            int cardsIdentified = 0;
            for (int i = discardCards.size() - 1; i >= 0; i--) {
                if (_additionalFilter.accepts(game, discardCards.get(i)) && cardsIdentified < _cardCount) {
                    if (physicalCard == discardCards.get(i)) {
                        return true;
                    }
                    cardsIdentified++;
                }
            }
            return false;
        } catch(PlayerNotFoundException exp) {
            game.sendErrorMessage(exp);
            return false;
        }
    }
}