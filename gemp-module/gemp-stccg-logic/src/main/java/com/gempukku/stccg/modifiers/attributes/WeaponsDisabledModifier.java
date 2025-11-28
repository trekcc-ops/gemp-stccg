package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.modifiers.AbstractModifier;
import com.gempukku.stccg.modifiers.ModifierEffect;

public class WeaponsDisabledModifier extends AbstractModifier {

    public WeaponsDisabledModifier(CardFilter affectedCards) {
        super("Weapons disabled while no matching personnel aboard", affectedCards, ModifierEffect.WEAPONS_DISABLED_MODIFIER);
    }

}