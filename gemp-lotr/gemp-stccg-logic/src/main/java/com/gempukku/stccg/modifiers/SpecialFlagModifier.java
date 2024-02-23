package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.condition.Condition;

public class SpecialFlagModifier extends AbstractModifier {
    private final ModifierFlag _modifierFlag;

    public SpecialFlagModifier(PhysicalCard source, ModifierFlag modifierFlag) {
        this(source, null, modifierFlag);
    }

    public SpecialFlagModifier(PhysicalCard source, Condition condition, ModifierFlag modifierFlag) {
        super(source, "Special flag set", null, condition, ModifierEffect.SPECIAL_FLAG_MODIFIER);
        _modifierFlag = modifierFlag;
    }

    @Override
    public boolean hasFlagActive(ModifierFlag modifierFlag) {
        return modifierFlag == _modifierFlag;
    }
}
