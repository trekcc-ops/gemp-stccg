package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.TribblesActionContext;

public interface TribblesTriggerChecker extends TriggerChecker {
    boolean isBefore();
    boolean accepts(TribblesActionContext actionContext);
}
