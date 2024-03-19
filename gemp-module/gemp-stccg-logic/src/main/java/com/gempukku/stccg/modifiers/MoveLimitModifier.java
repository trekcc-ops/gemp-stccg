package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class MoveLimitModifier extends AbstractModifier {
    private final int _moveLimitModifier;

    public MoveLimitModifier(PhysicalCard source, int moveLimitModifier) {
        super(source, null, null, null, ModifierEffect.MOVE_LIMIT_MODIFIER);
        _moveLimitModifier = moveLimitModifier;
    }

    @Override
    public int getMoveLimitModifier() {
        return _moveLimitModifier;
    }
}
