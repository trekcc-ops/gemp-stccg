package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effectappender.resolver.TimeResolver;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

public class AddUntilModifierEffect extends UnrespondableEffect {
    private final Modifier _modifier;
    private final TimeResolver.Time until;

    public AddUntilModifierEffect(DefaultGame game, Modifier modifier, TimeResolver.Time until) {
        super(game);
        _modifier = modifier;
        this.until = until;
    }

    @Override
    public void doPlayEffect() {
        Phase phase = until.getPhase();
        if (phase == null)
            phase = _game.getGameState().getCurrentPhase();

        if (until.isEndOfTurn())
            _game.getModifiersEnvironment().addUntilEndOfTurnModifier(_modifier);
        else if (until.isStart())
            _game.getModifiersEnvironment().addUntilStartOfPhaseModifier(_modifier, phase);
        else
            _game.getModifiersEnvironment().addUntilEndOfPhaseModifier(_modifier, phase);
    }
}
