package com.gempukku.lotro.filters;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.FilterableSource;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.game.DefaultGame;

public interface FilterableSourceProducer<AbstractGame extends DefaultGame> {
    FilterableSource<AbstractGame> createFilterableSource(String parameter, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
