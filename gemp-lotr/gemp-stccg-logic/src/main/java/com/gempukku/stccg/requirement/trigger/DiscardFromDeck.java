package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.actions.discard.DiscardCardFromDeckResult;
import org.json.simple.JSONObject;

public class DiscardFromDeck implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "filter", "memorize");

        final String filter = environment.getString(value.get("filter"), "filter", "any");
        final String memorize = environment.getString(value.get("memorize"), "memorize");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                boolean result = TriggerConditions.forEachDiscardedFromDeck(actionContext.getGame(), actionContext.getEffectResult(),
                        filterableSource.getFilterable(actionContext));
                if (result && memorize != null) {
                    actionContext.setCardMemory(memorize, ((DiscardCardFromDeckResult) actionContext.getEffectResult()).getDiscardedCard());
                }
                return result;
            }
        };
    }
}
