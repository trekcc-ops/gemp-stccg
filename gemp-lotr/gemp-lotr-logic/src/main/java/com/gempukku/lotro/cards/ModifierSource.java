package com.gempukku.lotro.cards;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.Modifier;

public interface ModifierSource {
    Modifier getModifier(DefaultActionContext<DefaultGame> actionContext);
}
