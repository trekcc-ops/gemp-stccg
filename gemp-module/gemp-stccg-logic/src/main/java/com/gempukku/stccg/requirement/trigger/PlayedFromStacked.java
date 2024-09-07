package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import org.json.simple.JSONObject;

public class PlayedFromStacked implements TriggerCheckerProducer {
    @Override
    public TriggerChecker getTriggerChecker(JSONObject value, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "filter", "from", "memorize");

        final String filterString = environment.getString(value.get("filter"), "filter");
        final String fromString = environment.getString(value.get("from"), "from");
        final String memorize = environment.getString(value.get("memorize"), "memorize");
        final FilterableSource filter = environment.getFilterFactory().generateFilter(filterString);
        final FilterableSource fromFilter = environment.getFilterFactory().generateFilter(fromString);
        return new TriggerChecker() {
            @Override
            public boolean accepts(ActionContext actionContext) {
                final Filterable filterable = filter.getFilterable(actionContext);
                final Filterable from = fromFilter.getFilterable(actionContext);
                final boolean played = TriggerConditions.playedFromStacked(actionContext.getGame(), actionContext.getEffectResult(), from, filterable);
                if (played && memorize != null) {
                    PhysicalCard playedCard = ((PlayCardResult) actionContext.getEffectResult()).getPlayedCard();
                    actionContext.setCardMemory(memorize, playedCard);
                }
                return played;
            }

            @Override
            public boolean isBefore() {
                return false;
            }
        };
    }
}
