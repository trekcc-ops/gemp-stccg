package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.game.DefaultGame;

public interface FilterableSource<AbstractGame extends DefaultGame> {
    Filterable getFilterable(DefaultActionContext<AbstractGame> actionContext);
}
