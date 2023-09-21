package com.gempukku.lotro.requirement;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.game.DefaultGame;

public interface Requirement<AbstractGame extends DefaultGame> {

    boolean accepts(DefaultActionContext<AbstractGame> actionContext);
}
