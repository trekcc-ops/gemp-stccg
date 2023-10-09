package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.game.DefaultGame;

public interface Requirement<AbstractGame extends DefaultGame> {

    boolean accepts(DefaultActionContext<AbstractGame> actionContext);
}
