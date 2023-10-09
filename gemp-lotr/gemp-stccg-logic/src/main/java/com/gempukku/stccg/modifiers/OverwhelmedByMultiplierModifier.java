package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class OverwhelmedByMultiplierModifier extends AbstractModifier {
    private final int _multiplier;

    public OverwhelmedByMultiplierModifier(PhysicalCard source, Filterable affectFilter, int multiplier) {
        this(source, affectFilter, null, multiplier);
    }

    public OverwhelmedByMultiplierModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int multiplier) {
        super(source, "Cannot be overwhelmed unless his strength is *" + multiplier, affectFilter, condition, ModifierEffect.OVERWHELM_MODIFIER);
        _multiplier = multiplier;
    }

    @Override
    public int getOverwhelmMultiplier(DefaultGame game, PhysicalCard physicalCard) {
        return _multiplier;
    }
}
