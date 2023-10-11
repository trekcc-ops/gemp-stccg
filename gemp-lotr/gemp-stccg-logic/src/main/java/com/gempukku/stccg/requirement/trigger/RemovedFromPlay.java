package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.results.DiscardCardsFromPlayResult;
import com.gempukku.stccg.results.ReturnCardsToHandResult;
import org.json.simple.JSONObject;

public class RemovedFromPlay implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter", "memorize");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");
        final String memorize = FieldUtils.getString(value.get("memorize"), "memorize");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filterableSource.getFilterable(actionContext);

                final boolean discardResult = TriggerConditions.forEachDiscardedFromPlay(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (discardResult && memorize != null) {
                    final PhysicalCard discardedCard = ((DiscardCardsFromPlayResult) actionContext.getEffectResult()).getDiscardedCard();
                    actionContext.setCardMemory(memorize, discardedCard);
                }

                final boolean returnedResult = TriggerConditions.forEachReturnedToHand(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (returnedResult && memorize != null) {
                    final PhysicalCard returnedCard = ((ReturnCardsToHandResult) actionContext.getEffectResult()).getReturnedCard();
                    actionContext.setCardMemory(memorize, returnedCard);
                }
                return discardResult || returnedResult;
            }
        };
    }
}
