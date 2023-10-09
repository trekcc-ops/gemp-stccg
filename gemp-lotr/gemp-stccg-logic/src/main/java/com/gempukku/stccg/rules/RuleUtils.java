package com.gempukku.stccg.rules;

import com.gempukku.stccg.cards.LotroCardBlueprint;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class RuleUtils {

    public static Filter getFullValidTargetFilter(String playerId, final DefaultGame game, final PhysicalCard self) {
        final LotroCardBlueprint blueprint = self.getBlueprint();
        return Filters.and(blueprint.getValidTargetFilter(playerId, game, self));
    }
}
