package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.common.Phase;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class ShouldSkipPhaseModifier extends AbstractModifier {
    private final Phase _phase;
    private final String _playerId;

    public ShouldSkipPhaseModifier(PhysicalCard source, Phase phase) {
        this(source, null, phase);
    }

    public ShouldSkipPhaseModifier(PhysicalCard source, Condition condition, Phase phase) {
        this(source, null, condition, phase);
    }

    public ShouldSkipPhaseModifier(PhysicalCard source, String playerId, Condition condition, Phase phase) {
        super(source, "Skip " + phase.toString() + " phase", null, condition, ModifierEffect.ACTION_MODIFIER);
        _playerId = playerId;
        _phase = phase;
    }

    @Override
    public boolean shouldSkipPhase(DefaultGame game, Phase phase, String playerId) {
        return phase == _phase && (_playerId == null || _playerId.equals(playerId));
    }
}