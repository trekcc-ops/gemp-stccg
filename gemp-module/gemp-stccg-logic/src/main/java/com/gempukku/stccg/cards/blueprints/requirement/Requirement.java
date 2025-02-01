package com.gempukku.stccg.cards.blueprints.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.PlayerNotFoundException;

public interface Requirement {
    boolean accepts(ActionContext actionContext);
}