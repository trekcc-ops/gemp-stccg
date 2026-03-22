package com.gempukku.stccg.modifiers.blueprints;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.ThisMissionCannotBeAttemptedModifier;

public class ThisMissionCannotBeAttemptedBlueprint implements ModifierBlueprint {
    @Override
    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, GameTextContext actionContext) {
        CardFilter affectedFilter = Filters.and(CardType.MISSION, Filters.atLocation(thisCard.getLocationId()));
        return new ThisMissionCannotBeAttemptedModifier(thisCard, affectedFilter);
    }
}