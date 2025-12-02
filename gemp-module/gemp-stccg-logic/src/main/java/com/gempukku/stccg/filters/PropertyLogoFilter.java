package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.PropertyLogo;
import com.gempukku.stccg.game.DefaultGame;

public class PropertyLogoFilter implements CardFilter {

    private final PropertyLogo _propertyLogo;

    public PropertyLogoFilter(PropertyLogo propertyLogo) {
        _propertyLogo = propertyLogo;
    }


    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getBlueprint().getPropertyLogo() == _propertyLogo;
    }
}