package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public class AddUntilEndOfPhaseModifierEffect extends UnrespondableEffect {
    private final Modifier _modifier;
    private final Phase _phase;
    private final DefaultGame _game;

    public AddUntilEndOfPhaseModifierEffect(DefaultGame game, Modifier modifier, Phase phase) {
        _modifier = modifier;
        _phase = phase;
        _game = game;
    }

    @Override
    public void doPlayEffect() {
        Phase phase = _phase;
        if (phase == null)
            phase = _game.getGameState().getCurrentPhase();
        _game.getModifiersEnvironment().addUntilEndOfPhaseModifier(_modifier, phase);
    }
}
