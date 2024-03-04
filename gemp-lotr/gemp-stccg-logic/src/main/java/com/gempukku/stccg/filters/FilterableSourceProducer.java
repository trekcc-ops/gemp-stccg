package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.CardBlueprintFactory;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface FilterableSourceProducer {
    FilterableSource createFilterableSource(String parameter, CardBlueprintFactory environment) throws InvalidCardDefinitionException;
}
