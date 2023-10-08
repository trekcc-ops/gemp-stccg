package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.fieldprocessor.FieldUtils;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.results.RevealCardFromTopOfDeckResult;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class RevealsCardFromTopOfDrawDeck implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(value, "filter");

        final String filter = FieldUtils.getString(value.get("filter"), "filter", "any");

        final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(filter, environment);

        return new TriggerChecker<>() {
            @Override
            public boolean isBefore() {
                return false;
            }

            @Override
            public boolean accepts(DefaultActionContext<DefaultGame> actionContext) {
                if (TriggerConditions.revealedCardsFromTopOfDeck(actionContext.getEffectResult(), actionContext.getPerformingPlayer())) {
                    RevealCardFromTopOfDeckResult revealCardFromTopOfDeckResult = (RevealCardFromTopOfDeckResult) actionContext.getEffectResult();
                    final Filterable filterable = filterableSource.getFilterable(actionContext);
                    final PhysicalCard revealedCard = revealCardFromTopOfDeckResult.getRevealedCard();
                    return Filters.and(filterable).accepts(actionContext.getGame(), revealedCard);
                }
                return false;
            }
        };
    }
}
