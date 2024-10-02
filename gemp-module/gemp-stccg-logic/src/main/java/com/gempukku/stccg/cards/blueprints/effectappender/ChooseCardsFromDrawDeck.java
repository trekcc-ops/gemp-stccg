package com.gempukku.stccg.cards.blueprints.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;

public class ChooseCardsFromDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "count", "filter", "memorize", "text");

        final String memorize = node.get("memorize").textValue();
        if (memorize == null)
            throw new InvalidCardDefinitionException("You need to define what memory to use to store chosen cards");
        final String text = environment.getString(node, "text", "Choose cards from deck.");

        return environment.buildTargetCardAppender(node, text, Zone.DRAW_DECK, memorize);

    }
}
