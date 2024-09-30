package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface FilterableSourceProducer {
    FilterableSource createFilterableSource(String parameter, CardBlueprintFactory environment) throws InvalidCardDefinitionException;
}
