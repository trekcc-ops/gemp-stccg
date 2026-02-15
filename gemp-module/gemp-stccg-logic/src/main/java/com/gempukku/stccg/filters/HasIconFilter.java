package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.game.DefaultGame;

public class HasIconFilter implements CardFilter {

    @JsonProperty("icon")
    private final CardIcon _icon;

    @JsonCreator
    public HasIconFilter(@JsonProperty(value = "icon", required = true) CardIcon icon) {
        _icon = icon;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.hasIcon(game, _icon);
    }
}