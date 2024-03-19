package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.lotr.Side;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class FreePeoplePlayerMayNotAssignCharacterModifier extends AbstractModifier {
    public FreePeoplePlayerMayNotAssignCharacterModifier(PhysicalCard source, Filterable affectFilter) {
        this(source, null, affectFilter);
    }

    public FreePeoplePlayerMayNotAssignCharacterModifier(PhysicalCard source, Condition condition, Filterable affectFilter) {
        super(source, "Free people player may not assign character to skirmish", affectFilter, condition, ModifierEffect.ASSIGNMENT_MODIFIER);
    }

    @Override
    public boolean isPreventedFromBeingAssignedToSkirmish(DefaultGame game, Side sidePlayer, PhysicalCard card) {
        return sidePlayer == Side.FREE_PEOPLE;
    }
}
