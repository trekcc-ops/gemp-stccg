package com.gempukku.lotro.cards.build.field.effect.filter;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FilterableSource;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.game.DefaultGame;

public interface FilterableSourceProducer<AbstractGame extends DefaultGame> {
    FilterableSource<AbstractGame> createFilterableSource(String parameter, CardGenerationEnvironment environment) throws InvalidCardDefinitionException;
}
