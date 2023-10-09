package com.gempukku.stccg.effects;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public class AddUntilEndOfTurnModifierEffect extends UnrespondableEffect {
    private final Modifier _modifier;

    public AddUntilEndOfTurnModifierEffect(Modifier modifier) {
        _modifier = modifier;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        game.getModifiersEnvironment().addUntilEndOfTurnModifier(_modifier);
    }
}
