package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.game.DefaultGame;

public class HasIconFilter implements CardFilter {

    private final CardIcon _icon;

    public HasIconFilter(CardIcon icon) {
        _icon = icon;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.hasIcon(game, _icon);
    }
}