package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.common.filterable.Zone;

public class ChooseCardsFromDiscard implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "filter", "memorize", "text");

        final String memorize = effectObject.get("memorize").textValue();
        if (memorize == null)
            throw new InvalidCardDefinitionException("You need to define what memory to use to store chosen cards");
        final String text = environment.getString(effectObject, "text", "Choose cards from discard.");

        return environment.buildTargetCardAppender(effectObject, text, Zone.DISCARD, memorize);
    }
}
