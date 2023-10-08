package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class CantBeTransferredModifier extends AbstractModifier {

    public CantBeTransferredModifier(PhysicalCard source, Filterable affectFilter, Condition condition) {
        super(source, "Can't be transferred", affectFilter, condition, ModifierEffect.TRANSFER_MODIFIER);
    }

    @Override
    public boolean canBeTransferred(DefaultGame game, PhysicalCard attachment) {
        return false;
    }
}
