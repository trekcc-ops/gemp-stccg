package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class CopyOfCardFilter implements CardFilter {

    @JsonProperty("blueprintId")
    private final String _blueprintId;

    @JsonCreator
    public CopyOfCardFilter(@JsonProperty(value = "blueprintId", required = true) String blueprintId) {
        _blueprintId = blueprintId;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return Objects.equals(physicalCard.getBlueprintId(), _blueprintId);
    }
}