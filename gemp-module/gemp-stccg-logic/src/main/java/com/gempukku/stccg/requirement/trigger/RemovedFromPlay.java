package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.actions.discard.DiscardCardsFromPlayResult;
import com.gempukku.stccg.actions.ReturnCardsToHandResult;

public class RemovedFromPlay implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode value, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {

        environment.validateAllowedFields(value, "filter", "memorize");
        final String memorize = value.get("memorize").textValue();
        final FilterableSource filterableSource = environment.getFilterable(value, "any");

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
