package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class RemoveGameTextModifier extends AbstractModifier {
    public RemoveGameTextModifier(PhysicalCard source, Filterable affectFilter) {
        this(source, null, affectFilter);
    }

    public RemoveGameTextModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "Has it's game text removed", affectFilter, condition, ModifierEffect.TEXT_MODIFIER);
    }

    @Override
    public boolean hasRemovedText(DefaultGame game, PhysicalCard physicalCard) {
        return true;
    }
}
