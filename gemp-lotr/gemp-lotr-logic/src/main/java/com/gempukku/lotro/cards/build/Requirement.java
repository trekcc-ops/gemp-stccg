package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.game.DefaultGame;

public interface Requirement<AbstractGame extends DefaultGame> {

    boolean accepts(DefaultActionContext<AbstractGame> actionContext);
}
