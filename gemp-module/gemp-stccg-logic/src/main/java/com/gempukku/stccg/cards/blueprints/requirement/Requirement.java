package com.gempukku.stccg.cards.blueprints.requirement;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface Requirement {
    boolean accepts(ActionContext actionContext) throws InvalidCardDefinitionException;
}