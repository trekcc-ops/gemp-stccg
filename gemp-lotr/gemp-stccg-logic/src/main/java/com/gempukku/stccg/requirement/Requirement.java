package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;

public interface Requirement<AbstractContext extends ActionContext> {
    boolean accepts(AbstractContext actionContext);
}
