package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.JsonViews;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "cardCount", "cardIds" })
@JsonPropertyOrder({ "cardCount", "cardIds" })
@JsonView(JsonViews.Public.class)
public class PhysicalCardGroup {
    private final List<PhysicalCard> _cards = new LinkedList<>();

    public PhysicalCardGroup() {
    }

    public void addCard(PhysicalCard<? extends DefaultGame> card) { _cards.add(card); }

    @JsonProperty("cardIds")
    @JsonIdentityReference(alwaysAsId=true)
    public List<PhysicalCard> getCards() {
        return _cards;
    }
    public void setCards(List<PhysicalCard> subDeck) {
        _cards.clear();
        _cards.addAll(subDeck);
    }

    @JsonProperty("cardCount")
    public int size() {
        return _cards.size();
    }

    public PhysicalCard getFirst() {
        return _cards.getFirst();
    }
}