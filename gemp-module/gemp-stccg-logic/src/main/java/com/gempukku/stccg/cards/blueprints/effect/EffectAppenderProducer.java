package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;

public interface EffectAppenderProducer {
    EffectBlueprint createEffectAppender(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException;

}