package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.ActionContext;

public interface DefaultRequirement extends Requirement<ActionContext> {
    boolean accepts(ActionContext actionContext);
}