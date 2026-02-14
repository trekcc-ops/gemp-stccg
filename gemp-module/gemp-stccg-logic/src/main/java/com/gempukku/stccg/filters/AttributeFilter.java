package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.DefaultGame;

public class AttributeFilter implements CardFilter {

    @JsonProperty("attribute")
    private final CardAttribute _attribute;

    @JsonProperty("comparatorType")
    private final ComparatorType _comparatorType;

    @JsonProperty("amountComparingTo")
    private final int _amountComparingTo;

    public AttributeFilter(@JsonProperty("attribute") CardAttribute attribute,
                           @JsonProperty("comparatorType") ComparatorType comparatorType,
                           @JsonProperty("amountComparingTo") int amountComparingTo) {
        _attribute = attribute;
        _comparatorType = comparatorType;
        _amountComparingTo = amountComparingTo;
    }

   @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        int amountComparing = game.getAttribute(physicalCard, _attribute);
        return _comparatorType.isTrue(amountComparing, _amountComparingTo);
    }
}