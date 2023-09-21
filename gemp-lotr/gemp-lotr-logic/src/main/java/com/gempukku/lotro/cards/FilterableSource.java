package com.gempukku.lotro.cards;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.DefaultGame;

public interface FilterableSource<AbstractGame extends DefaultGame> {
    Filterable getFilterable(DefaultActionContext<AbstractGame> actionContext);
}
