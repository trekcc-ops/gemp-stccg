package com.gempukku.stccg.filters;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.lotronly.LotroPlayUtils;

public class ExtraFilters {
    public static Filter attachableTo(final DefaultGame game, final Filterable... filters) {
        return attachableTo(game, 0, filters);
    }

    public static Filter attachableTo(final DefaultGame game, final int twilightModifier, final Filterable... filters) {
        return Filters.and(Filters.playable(game, twilightModifier),
                (Filter) (game1, physicalCard) -> {
                    if (physicalCard.getBlueprint().getValidTargetFilter(physicalCard.getOwner(), game1, physicalCard) == null)
                        return false;
                    return LotroPlayUtils.checkPlayRequirements(game1, physicalCard, Filters.and(filters), 0, twilightModifier, false, false, true);
                });
    }
}
