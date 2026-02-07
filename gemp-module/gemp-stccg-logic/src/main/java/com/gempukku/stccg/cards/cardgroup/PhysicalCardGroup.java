package com.gempukku.stccg.cards.cardgroup;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.util.LinkedList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "cardIds" })
@JsonPropertyOrder({ "cardIds" })
@JsonIgnoreProperties(value = { "cardCount" }, allowGetters = true)
public class PhysicalCardGroup<T extends PhysicalCard> {
    protected final List<T> _cards = new LinkedList<>();

    public PhysicalCardGroup() {

    }

    public PhysicalCardGroup(@JsonProperty("cardIds")
                             @JsonIdentityReference(alwaysAsId=true)
                             List<T> cards) {
        _cards.addAll(cards);
    }

    public void addCard(T card) { _cards.add(card); }

    @JsonProperty("cardIds")
    @JsonIdentityReference(alwaysAsId=true)
    public final List<T> getCards() {
        return _cards;
    }
    public void setCards(List<T> subDeck) {
        _cards.clear();
        _cards.addAll(subDeck);
    }

    @JsonIgnore
    public int size() {
        return _cards.size();
    }

    public void remove(PhysicalCard card) { _cards.remove(card); }

    public PhysicalCard getFirst() {
        return _cards.getFirst();
    }

}