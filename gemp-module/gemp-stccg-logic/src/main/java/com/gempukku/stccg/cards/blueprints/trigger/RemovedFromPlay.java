package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.placecard.ReturnCardsToHandResult;
import com.gempukku.stccg.actions.discard.DiscardCardFromPlayResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;

public class RemovedFromPlay implements TriggerCheckerProducer {
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
                final Filterable filterable = filterableSource.getFilterable(actionContext);

                final boolean discardResult = TriggerConditions.forEachDiscardedFromPlay(actionContext.getGame(), actionContext.getEffectResult(), filterable);
                if (discardResult && memorize != null) {
                    final PhysicalCard discardedCard = ((DiscardCardFromPlayResult) actionContext.getEffectResult()).getDiscardedCard();
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