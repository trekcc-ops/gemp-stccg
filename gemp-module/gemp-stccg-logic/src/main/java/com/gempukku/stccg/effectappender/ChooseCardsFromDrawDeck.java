package com.gempukku.stccg.effectappender;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;

public class ChooseCardsFromDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "count", "filter", "memorize", "text");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(node.get("count"), 1, environment);
        final String filter = environment.getString(node, "filter", "choose(any)");
        final String memorize = node.get("memorize").textValue();
        if (memorize == null)
            throw new InvalidCardDefinitionException("You need to define what memory to use to store chosen cards");
        final String text = environment.getString(node, "text", "Choose cards from deck.");

        return CardResolver.resolveCardsInZone(filter, null, valueSource, memorize,
                PlayerResolver.resolvePlayer("you"), text, environment, Zone.DRAW_DECK);
    }
}
