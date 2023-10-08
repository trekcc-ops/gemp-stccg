package com.gempukku.stccg.effects;

import com.gempukku.stccg.effectappender.resolver.TimeResolver;
import com.gempukku.stccg.common.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public class AddUntilModifierEffect extends UnrespondableEffect {
    private final Modifier _modifier;
    private final TimeResolver.Time until;

    public AddUntilModifierEffect(Modifier modifier, TimeResolver.Time until) {
        _modifier = modifier;
        this.until = until;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        Phase phase = until.getPhase();
        if (phase == null)
            phase = game.getGameState().getCurrentPhase();

        if (until.isEndOfTurn())
            game.getModifiersEnvironment().addUntilEndOfTurnModifier(_modifier);
        else if (until.isStart())
            game.getModifiersEnvironment().addUntilStartOfPhaseModifier(_modifier, phase);
        else
            game.getModifiersEnvironment().addUntilEndOfPhaseModifier(_modifier, phase);
    }
}
