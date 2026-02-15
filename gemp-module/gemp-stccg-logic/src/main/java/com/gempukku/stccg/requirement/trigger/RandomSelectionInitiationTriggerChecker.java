package com.gempukku.stccg.requirement.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.choose.RandomSelectionInitiatedResult;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;

public class RandomSelectionInitiationTriggerChecker implements TriggerChecker {

    private final FilterBlueprint _includedCardFilter;

    private RandomSelectionInitiationTriggerChecker(
            @JsonProperty("selectionIncludes") FilterBlueprint includedCardFilter) {
        _includedCardFilter = includedCardFilter;
    }

    @Override
    public boolean accepts(ActionContext context, DefaultGame cardGame) {
        ActionResult currentResult = cardGame.getCurrentActionResult();
        if (currentResult != null && currentResult.hasType(ActionResult.Type.RANDOM_SELECTION_INITIATED) &&
                currentResult instanceof RandomSelectionInitiatedResult randomResult) {
            CardFilter eligibleCardFilter = _includedCardFilter.getFilterable(cardGame, context);
            return randomResult.includesCardMatchingFilter(cardGame, eligibleCardFilter);
        }
        return false;
    }
}