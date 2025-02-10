package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.JsonViews;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gameevent.GameEvent;
import com.gempukku.stccg.gameevent.GameStateListener;
import com.gempukku.stccg.gameevent.RemoveCardsFromPlayGameEvent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DiscardPile extends CardPile {

    @JsonProperty("cardCount")
    public int size() {
        return _cards.size();
    }

    public void remove(PhysicalCard card) {
        _cards.remove(card);
        card.setZone(Zone.VOID);
    }

}