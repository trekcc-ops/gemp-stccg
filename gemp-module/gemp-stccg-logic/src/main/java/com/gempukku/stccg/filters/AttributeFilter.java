package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Collections;

public class AttributeFilter implements CardFilter {

    @JsonProperty("attributes")
    private final Collection<CardAttribute> _attributes;

    @JsonProperty("comparatorType")
    private final ComparatorType _comparatorType;

    @JsonProperty("amountComparingTo")
    private final int _amountComparingTo;

    @JsonCreator
    public AttributeFilter(@JsonProperty("attribute") CardAttribute attribute,
                           @JsonProperty("comparatorType") ComparatorType comparatorType,
                           @JsonProperty("amountComparingTo") int amountComparingTo) {
        _attributes = Collections.singleton(attribute);
        _comparatorType = comparatorType;
        _amountComparingTo = amountComparingTo;
    }

    public AttributeFilter(Collection<CardAttribute> attributes, ComparatorType comparatorType, int amountComparingTo) {
        _attributes = attributes;
        _comparatorType = comparatorType;
        _amountComparingTo = amountComparingTo;
    }

   @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        int amountComparing = 0;
        for (CardAttribute attribute : _attributes) {
            amountComparing = amountComparing + game.getAttribute(physicalCard, attribute);
        }
        return _comparatorType.isTrue(amountComparing, _amountComparingTo);
    }
}