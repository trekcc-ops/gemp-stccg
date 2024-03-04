package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.actions.revealcards.RevealCardFromTopOfDeckResult;
import org.json.simple.JSONObject;

public class RevealsCardFromTopOfDrawDeck implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "filter");

        final FilterableSource filterableSource = environment.getFilterable(value, "any");

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
