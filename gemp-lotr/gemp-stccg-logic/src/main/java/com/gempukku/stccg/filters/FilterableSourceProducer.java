package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface FilterableSourceProducer {
    FilterableSource createFilterableSource(String parameter, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
