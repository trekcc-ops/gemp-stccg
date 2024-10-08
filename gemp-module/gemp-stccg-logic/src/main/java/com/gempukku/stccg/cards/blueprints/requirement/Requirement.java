package com.gempukku.stccg.cards.blueprints.requirement;

import com.gempukku.stccg.cards.ActionContext;

public interface Requirement {
    boolean accepts(ActionContext actionContext);
}