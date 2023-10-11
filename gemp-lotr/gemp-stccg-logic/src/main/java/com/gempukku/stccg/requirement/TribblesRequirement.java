package com.gempukku.stccg.requirement;

import com.gempukku.stccg.cards.TribblesActionContext;

public interface TribblesRequirement extends Requirement<TribblesActionContext> {
    boolean accepts(TribblesActionContext actionContext);
}