package com.gempukku.stccg.cards;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public interface ModifierSource {
    Modifier getModifier(DefaultActionContext<DefaultGame> actionContext);
}
