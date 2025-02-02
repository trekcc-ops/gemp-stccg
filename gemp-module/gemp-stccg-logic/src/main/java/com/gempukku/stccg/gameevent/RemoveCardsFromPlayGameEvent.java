package com.gempukku.stccg.gameevent;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.Collection;
import java.util.Set;

public class RemoveCardsFromPlayGameEvent extends GameEvent {

    public RemoveCardsFromPlayGameEvent(DefaultGame cardGame, Set<PhysicalCard> visibleRemovedCards,
                                        Player performingPlayer) {
        super(cardGame, GameEvent.Type.REMOVE_CARD_FROM_PLAY, performingPlayer);
        setOtherCards(visibleRemovedCards);
    }

    private void setOtherCards(Collection<PhysicalCard> cards) {
        int[] otherCardIds = new int[cards.size()];
        int index = 0;
        for (PhysicalCard card : cards) {
            otherCardIds[index] = card.getCardId();
            index++;
        }
        _eventAttributes.put(Attribute.otherCardIds, TextUtils.arrayToCommaSeparated(otherCardIds));
    }
}