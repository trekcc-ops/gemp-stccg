package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;

public interface Requirement {
    boolean accepts(ActionContext actionContext);
}
