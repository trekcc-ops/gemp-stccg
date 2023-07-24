package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;

import java.util.Collection;

public class CantWoundWithCardModifier extends AbstractModifier {
    private final Filter _preventWoundWithFilter;

    public CantWoundWithCardModifier(LotroPhysicalCard source, Filterable affectFilter, Filterable preventWoundWithFilter) {
        this(source, affectFilter, null, preventWoundWithFilter);
    }

    public CantWoundWithCardModifier(LotroPhysicalCard source, Filterable affectFilter, Condition condition, Filterable preventWoundWithFilter) {
        super(source, "Affected by wound preventing effect", affectFilter, condition, ModifierEffect.WOUND_MODIFIER);
        _preventWoundWithFilter = Filters.and(preventWoundWithFilter);
    }

    @Override
    public boolean canTakeWounds(DefaultGame game, Collection<LotroPhysicalCard> woundSources, LotroPhysicalCard physicalCard, int woundsAlreadyTaken, int woundsToTake) {
        for (LotroPhysicalCard woundSource : woundSources) {
            if (_preventWoundWithFilter.accepts(game, woundSource))
                return false;
        }

        return true;
    }
}
