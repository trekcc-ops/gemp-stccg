package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.discard.DiscardCardFromDeckResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterableSource;

public class DiscardFromDeck implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value)
            throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(value, "filter", "memorize");

        final String memorize = value.get("memorize").textValue();
        final FilterableSource filterableSource = BlueprintUtils.getFilterable(value, "any");

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                boolean result = TriggerConditions.forEachDiscardedFromDeck(
                        actionContext.getGame(), actionContext.getEffectResult(),
                        filterableSource.getFilterable(actionContext));
                if (result && memorize != null) {
                    actionContext.setCardMemory(memorize,
                            ((DiscardCardFromDeckResult) actionContext.getEffectResult()).getDiscardedCard());
                }
                return result;
            }
        };
    }
}