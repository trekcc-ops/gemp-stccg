package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import org.json.simple.JSONObject;

public class ChooseCardsFromDrawDeck implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(effectObject, "count", "filter", "memorize", "text");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("count"), 1, environment);
        final String filter = environment.getString(effectObject.get("filter"), "filter", "choose(any)");
        final String memorize = environment.getString(effectObject.get("memorize"), "memorize");
        if (memorize == null)
            throw new InvalidCardDefinitionException("You need to define what memory to use to store chosen cards");
        final String text = environment.getString(effectObject.get("text"), "text", "Choose cards from deck.");

        return CardResolver.resolveCardsInZone(filter, null, valueSource, memorize,
                PlayerResolver.resolvePlayer("you"), text, environment, Zone.DRAW_DECK);
    }
}