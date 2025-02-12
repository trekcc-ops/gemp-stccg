package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.discard.DiscardCardFromDeckResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class DiscardedFromDeckTriggerChecker implements TriggerChecker {

    private final FilterBlueprint _filterBlueprint;
    private final String _memoryId;

    DiscardedFromDeckTriggerChecker(@JsonProperty("filter")
                    FilterBlueprint filterBlueprint,
                                    @JsonProperty("memorize")
                    String memoryId) throws InvalidCardDefinitionException {
        _memoryId = memoryId;
        _filterBlueprint = Objects.requireNonNullElse(filterBlueprint, actionContext -> Filters.any);
    }


    private static boolean forEachDiscardedFromDeck(DefaultGame game, ActionResult actionResult, Filterable... filters) {
        if (actionResult.getType() == ActionResult.Type.FOR_EACH_DISCARDED_FROM_DECK)
            return Filters.and(filters).accepts(game, ((DiscardCardFromDeckResult) actionResult).getDiscardedCard());
        return false;
    }

    @Override
    public boolean accepts(ActionContext actionContext) {
        boolean result = forEachDiscardedFromDeck(
                actionContext.getGame(), actionContext.getEffectResult(),
                _filterBlueprint.getFilterable(actionContext));
        if (result && _memoryId != null) {
            actionContext.setCardMemory(_memoryId,
                    ((DiscardCardFromDeckResult) actionContext.getEffectResult()).getDiscardedCard());
        }
        return result;
    }

}