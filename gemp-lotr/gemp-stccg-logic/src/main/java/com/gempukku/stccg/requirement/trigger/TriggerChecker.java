package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.requirement.Requirement;

public interface TriggerChecker<AbstractContext extends ActionContext> extends Requirement<AbstractContext> {
    boolean isBefore();

    boolean accepts(AbstractContext actionContext);
}
