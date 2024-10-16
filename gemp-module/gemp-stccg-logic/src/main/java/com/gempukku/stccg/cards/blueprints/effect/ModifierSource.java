package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.modifiers.Modifier;

public interface ModifierSource {
    Modifier getModifier(ActionContext actionContext);
}