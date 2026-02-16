package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.TrueCondition;

public class ThisMissionCannotBeAttemptedModifier extends AbstractModifier {

    public ThisMissionCannotBeAttemptedModifier(PhysicalCard performingCard, CardFilter affectedFilter) {
        super(performingCard, affectedFilter, new TrueCondition(), ModifierEffect.MISSION_ATTEMPT_MODIFIER,
                false);
    }
    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return "Mission cannot be attempted";
    }
}