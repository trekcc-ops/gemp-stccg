package com.gempukku.stccg.filters;

import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public class MatchingFilterBlueprint implements FilterBlueprint {

    private final ActionCardResolver _cardTarget;

    public MatchingFilterBlueprint(ActionCardResolver cardTarget) {
        _cardTarget = cardTarget;
    }

    public Filterable getFilterable(ActionContext actionContext) {
        try {
            return Filters.matchingAffiliation(_cardTarget.getCards(actionContext.getGame()));
        } catch(InvalidGameLogicException exp) {
            actionContext.getGame().sendErrorMessage(exp);
            return null;
        }
    }
}