package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public class AddUntilStartOfPhaseModifierEffect extends UnrespondableEffect {
    private final Modifier _modifier;
    private final Phase _phase;

    public AddUntilStartOfPhaseModifierEffect(DefaultGame game, Modifier modifier, Phase phase) {
        super(game);
        _modifier = modifier;
        _phase = phase;
    }

    @Override
    public void doPlayEffect() {
        _game.getModifiersEnvironment().addUntilStartOfPhaseModifier(_modifier, _phase);
    }
}
