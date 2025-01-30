package com.gempukku.stccg.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

public interface FilterableSourceProducer {
    FilterBlueprint createFilterableSource(String parameter) throws InvalidCardDefinitionException, JsonProcessingException;
}