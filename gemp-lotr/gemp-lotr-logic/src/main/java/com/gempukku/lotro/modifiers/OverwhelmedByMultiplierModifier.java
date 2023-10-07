package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.modifiers.ModifierEffect;
import com.gempukku.lotro.condition.Condition;

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
