package com.gempukku.stccg.filters;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public interface Filter extends Filterable {
    boolean accepts(DefaultGame game, PhysicalCard physicalCard);
}
