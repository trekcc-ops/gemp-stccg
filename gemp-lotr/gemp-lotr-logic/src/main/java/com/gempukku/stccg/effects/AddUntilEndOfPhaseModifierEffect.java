package com.gempukku.stccg.effects;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public class AddUntilEndOfPhaseModifierEffect extends UnrespondableEffect {
    private final Modifier _modifier;
    private final Phase _phase;

    public AddUntilEndOfPhaseModifierEffect(Modifier modifier) {
        this(modifier, null);
    }

    public AddUntilEndOfPhaseModifierEffect(Modifier modifier, Phase phase) {
        _modifier = modifier;
        _phase = phase;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        Phase phase = _phase;
        if (phase == null)
            phase = game.getGameState().getCurrentPhase();
        game.getModifiersEnvironment().addUntilEndOfPhaseModifier(_modifier, phase);
    }
}
