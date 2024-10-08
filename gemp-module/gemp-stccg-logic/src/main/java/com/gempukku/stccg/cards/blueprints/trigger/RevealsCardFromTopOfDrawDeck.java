package com.gempukku.stccg.cards.blueprints.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.actions.revealcards.RevealCardFromTopOfDeckResult;

public class RevealsCardFromTopOfDrawDeck implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "filter");

        final FilterableSource filterableSource = environment.getFilterable(node, "any");

        return new TriggerChecker() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(ActionContext actionContext) {
                if (TriggerConditions.revealedCardsFromTopOfDeck(
                        actionContext.getEffectResult(), actionContext.getPerformingPlayerId())) {
                    return Filters.and(filterableSource.getFilterable(actionContext)).accepts(
                            actionContext.getGame(),
                            ((RevealCardFromTopOfDeckResult) actionContext.getEffectResult()).getRevealedCard()
                    );
                }
                return false;
            }
        };
    }
}