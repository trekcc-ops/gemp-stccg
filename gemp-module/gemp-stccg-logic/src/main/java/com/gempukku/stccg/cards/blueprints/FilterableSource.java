package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.PlayerNotFoundException;

public interface FilterableSource {
    Filterable getFilterable(ActionContext actionContext);
}