package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

import java.util.Set;

public class CantTakeWoundsFromLosingSkirmishModifier extends AbstractModifier {
    private final Filter _winnersFilter;

    public CantTakeWoundsFromLosingSkirmishModifier(PhysicalCard source, Filterable affectFilter, Filterable winnersFilter) {
        super(source, "Can't take wounds", affectFilter, ModifierEffect.WOUND_MODIFIER);
        _winnersFilter = Filters.and(winnersFilter);
    }

    @Override
    public boolean canTakeWoundsFromLosingSkirmish(DefaultGame game, PhysicalCard physicalCard, Set<PhysicalCard> winners) {
        if (_winnersFilter == null
                || Filters.filter(winners, game, _winnersFilter).size() > 0)
            return false;
        return true;
    }
}