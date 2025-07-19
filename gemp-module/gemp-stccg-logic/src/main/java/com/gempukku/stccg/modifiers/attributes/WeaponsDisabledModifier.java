package com.gempukku.stccg.modifiers.attributes;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.condition.TrueCondition;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.modifiers.AbstractModifier;
import com.gempukku.stccg.modifiers.ModifierEffect;

public class WeaponsDisabledModifier extends AbstractModifier {

    public WeaponsDisabledModifier(CardFilter affectedCards) {
        super("Weapons disabled while no matching personnel aboard", affectedCards, ModifierEffect.WEAPONS_DISABLED_MODIFIER);
    }

}