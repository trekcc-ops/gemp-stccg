package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;

public interface FilterableSourceProducer<AbstractGame extends DefaultGame> {
    FilterableSource<AbstractGame> createFilterableSource(String parameter, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
