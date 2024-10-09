package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class CancelStrengthBonusSourceModifier extends AbstractModifier {

    public CancelStrengthBonusSourceModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "Cancel strength bonus", affectFilter, condition, ModifierEffect.STRENGTH_BONUS_SOURCE_MODIFIER);
    }

    @Override
    public boolean cancelsStrengthBonusModifier(DefaultGame game, PhysicalCard modifierSource, PhysicalCard modifierTarget) {
        return true;
    }
}
