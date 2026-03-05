package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Condition;

import java.util.ArrayList;
import java.util.Collection;

public class AddCardPlayDestinationModifier extends AbstractModifier {

    private final CardFilter _destinationFilter;

    public AddCardPlayDestinationModifier(PhysicalCard source, CardFilter affectedCards, Condition condition,
                                             CardFilter destinationFilter) {
        super(source, affectedCards, condition, ModifierEffect.ADD_DESTINATION, false);
        _destinationFilter = destinationFilter;
    }

    @Override
    public String getCardInfoText(DefaultGame cardGame, PhysicalCard affectedCard) {
        return null;
    }

    public Collection<PhysicalCard> getAdditionalDestinationOptions(DefaultGame cardGame, PhysicalCard affectedCard) {
        Collection<PhysicalCard> result = new ArrayList<>();
        if (_affectedCardsFilter.accepts(cardGame, affectedCard)) {
            result.addAll(Filters.filterCardsInPlay(cardGame, _destinationFilter));
        }
        return result;
    }
}