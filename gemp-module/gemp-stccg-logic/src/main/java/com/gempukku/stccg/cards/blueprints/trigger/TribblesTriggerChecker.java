package com.gempukku.stccg.cards.blueprints.trigger;

import com.gempukku.stccg.cards.TribblesActionContext;

public interface TribblesTriggerChecker extends TriggerChecker {
    boolean isBefore();
    boolean accepts(TribblesActionContext actionContext);
}